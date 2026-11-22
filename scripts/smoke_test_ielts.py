#!/usr/bin/env python3
"""End-to-end smoke test for all four IELTS exam skills."""

from __future__ import annotations

import argparse
from urllib.parse import urlparse

import requests


SKILLS = ("LISTENING", "READING", "WRITING", "SPEAKING")


def ok(response: requests.Response) -> dict:
    response.raise_for_status()
    payload = response.json()
    if payload.get("code") not in (200, 201):
        raise AssertionError(payload)
    return payload


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8081/api")
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", default="admin")
    args = parser.parse_args()
    base = args.base_url.rstrip("/")

    session = requests.Session()
    login = ok(
        session.post(
            f"{base}/auth/login",
            json={"username": args.username, "password": args.password},
            timeout=20,
        )
    )["result"]
    session.headers["Authorization"] = f"Bearer {login['token']}"

    exams = ok(
        session.get(
            f"{base}/exams/list",
            params={"pageNo": 1, "pageSize": 20, "sortBy": "id:desc"},
            timeout=20,
        )
    )["result"]["items"]
    exam = next(item for item in exams if item["title"].startswith("[IELTS Academic Full Mock]"))
    parts_by_skill = {part["skillType"]: part for part in exam["examParts"]}
    if set(parts_by_skill) != set(SKILLS):
        raise AssertionError(f"Expected four skills, got {sorted(parts_by_skill)}")

    results: dict[str, int] = {}
    for skill in SKILLS:
        attempt = ok(
            session.post(
                f"{base}/userexams",
                json={"examId": exam["id"], "skillType": skill},
                timeout=20,
            )
        )["result"]
        if len(attempt.get("parts") or []) != 1:
            raise AssertionError(f"{skill}: expected one attempt part, got {attempt}")
        user_exam_part_id = attempt["parts"][0]["id"]
        questions = ok(
            session.get(
                f"{base}/questions/list",
                params={
                    "examPartId": parts_by_skill[skill]["id"],
                    "pageNo": 0,
                    "pageSize": 100,
                    "sortBy": "id:asc",
                },
                timeout=30,
            )
        )["result"]["items"]
        if not questions:
            raise AssertionError(f"{skill}: no questions")
        if skill in {"LISTENING", "READING"} and len(questions) != 40:
            raise AssertionError(f"{skill}: IELTS mock must contain 40 questions")
        results[skill] = len(questions)

        if skill == "LISTENING":
            audio_url = questions[0].get("audioUrl")
            if not audio_url:
                raise AssertionError("Listening question has no audio URL")
            parsed = urlparse(audio_url)
            host_audio_url = parsed._replace(netloc="localhost:8081").geturl()
            # ExoPlayer loads media directly, without Retrofit's bearer token.
            audio_response = requests.get(host_audio_url, stream=True, timeout=20)
            audio_response.raise_for_status()
            first_chunk = next(audio_response.iter_content(4096), b"")
            audio_response.close()
            if not first_chunk:
                raise AssertionError("Listening audio is empty")

        if skill == "READING":
            tfng = next(
                (
                    question
                    for question in questions
                    if "TRUE, FALSE, or NOT GIVEN" in question.get("content", "")
                ),
                None,
            )
            if tfng is None:
                raise AssertionError("Reading test has no TRUE/FALSE/NOT GIVEN item")
            option_texts = {option["content"] for option in tfng.get("optionRes") or []}
            if option_texts != {"TRUE", "FALSE", "NOT GIVEN"}:
                raise AssertionError(f"Invalid TRUE/FALSE/NOT GIVEN options: {option_texts}")

        if skill in {"LISTENING", "READING"}:
            answers = [
                {"questionId": question["id"], "selectedOptionId": question["correctOptionId"]}
                for question in questions
            ]
            submission = ok(
                session.post(
                    f"{base}/userexamparts",
                    json={"userExamPartId": user_exam_part_id, "answers": answers},
                    timeout=30,
                )
            )["result"]
            if float(submission.get("score", 0)) <= 0:
                raise AssertionError(f"{skill}: invalid score response {submission}")

        if skill == "WRITING":
            task_results = []
            for question in questions:
                band = 9.0 if question.get("section", "").lower() == "task2" else 6.0
                task_results.append(
                    {
                        "questionId": question["id"],
                        "essayText": "Backend weighting smoke test.",
                        "wordCount": 4,
                        "duration": 1,
                        "overallBand": band,
                        "taskAchievement": band,
                        "coherenceCohesion": band,
                        "lexicalResource": band,
                        "grammaticalRange": band,
                        "detailedFeedback": "Smoke test",
                        "examinerFeedback": "Smoke test",
                    }
                )
            writing_submission = ok(
                session.post(
                    f"{base}/userexamparts/writing/submit",
                    json={
                        "userExamPartId": user_exam_part_id,
                        # Deliberately wrong: backend must ignore this value.
                        "overallBand": 1.0,
                        "taskResults": task_results,
                    },
                    timeout=30,
                )
            )["result"]
            if float(writing_submission.get("overallBand", -1)) != 8.0:
                raise AssertionError(
                    "Writing weighting failed: expected (6 + 2*9)/3 = 8.0, "
                    f"got {writing_submission}"
                )

    full_attempt = ok(
        session.post(
            f"{base}/userexams",
            json={"examId": exam["id"], "skillType": None},
            timeout=20,
        )
    )["result"]
    if len(full_attempt.get("parts") or []) != 4:
        raise AssertionError("Full test did not create four user exam parts")

    print(f"PASS {exam['title']} (id={exam['id']})")
    for skill in SKILLS:
        print(f"{skill}: {results[skill]} questions")
    print("FULL TEST: 4/4 parts created")


if __name__ == "__main__":
    main()
