#!/usr/bin/env python3
"""Smoke-test Writing and optional Speaking AI grading through the backend."""

from __future__ import annotations

import argparse
from pathlib import Path

import requests


ESSAY = """
The graph illustrates a clear increase in renewable electricity generation in
all four regions over the period shown. Overall, Europe experienced the most
substantial growth and finished with the highest proportion, while Asia
remained the lowest producer despite a steady improvement.

In 2000, South America generated the largest share of renewable electricity,
at approximately thirty-five percent. Europe followed at fifteen percent,
whereas North America and Asia stood at roughly ten and five percent
respectively. During the following decade, Europe rose consistently to around
twenty-five percent. In contrast, South America's figure declined moderately
to about thirty percent before it began to recover.

The second half of the period saw faster growth in most regions. Europe's
percentage climbed sharply and reached about forty-five percent in 2020,
overtaking South America, which recovered to forty percent. North America was
comparatively stable during the first decade, but subsequently doubled from
around ten to twenty percent. Asia also increased, although at a slower pace,
ending at approximately fifteen percent. Thus, the gap between Europe and Asia
became considerably wider by the end of the period, even though every region
relied more heavily on renewable sources than it had at the beginning.
""".strip()


def ok(response: requests.Response) -> dict:
    if not response.ok:
        raise AssertionError(
            f"HTTP {response.status_code} {response.request.method} {response.url}: "
            f"{response.text[:2000]}"
        )
    payload = response.json()
    # Most endpoints omit `code`; the speaking wrapper uses ApiResponse's
    # default success code 0.
    if payload.get("code", 200) not in (0, 200, 201):
        raise AssertionError(payload)
    return payload


def questions(session: requests.Session, base: str, exam_part_id: int) -> list[dict]:
    return ok(
        session.get(
            f"{base}/questions/list",
            params={"examPartId": exam_part_id, "pageNo": 0, "pageSize": 100},
            timeout=30,
        )
    )["result"]["items"]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8081/api")
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", default="admin")
    parser.add_argument("--audio", type=Path)
    args = parser.parse_args()
    base = args.base_url.rstrip("/")

    session = requests.Session()
    token = ok(
        session.post(
            f"{base}/auth/login",
            json={"username": args.username, "password": args.password},
            timeout=20,
        )
    )["result"]["token"]
    session.headers["Authorization"] = f"Bearer {token}"

    exam = ok(
        session.get(
            f"{base}/exams/list",
            params={"pageNo": 1, "pageSize": 10, "sortBy": "id:desc"},
            timeout=20,
        )
    )["result"]["items"][0]
    parts = {part["skillType"]: part for part in exam["examParts"]}

    writing_question = questions(session, base, parts["WRITING"]["id"])[0]
    writing = ok(
        session.post(
            f"{base}/ielts/writing/evaluate-by-question",
            data={
                "essayText": ESSAY,
                "questionId": writing_question["id"],
                "wordCount": len(ESSAY.split()),
                "duration": 20,
            },
            timeout=180,
        )
    )["result"]
    writing_band = writing.get("overall_band")
    writing_feedback = writing.get("examiner_feedback")
    if not isinstance(writing_band, (int, float)) or not 0 <= writing_band <= 9:
        raise AssertionError(f"Invalid Writing band: {writing}")
    if not writing_feedback:
        raise AssertionError(f"Missing Writing feedback: {writing}")
    print(
        f"WRITING PASS: overall_band={writing_band} "
        f"model_feedback={bool(writing_feedback)}"
    )

    if args.audio:
        speaking_attempt = ok(
            session.post(
                f"{base}/userexams",
                json={"examId": exam["id"], "skillType": "SPEAKING"},
                timeout=20,
            )
        )["result"]
        user_exam_part_id = speaking_attempt["parts"][0]["id"]
        speaking_question = questions(session, base, parts["SPEAKING"]["id"])[0]
        with args.audio.open("rb") as audio_handle:
            speaking = ok(
                session.post(
                    f"{base}/ielts/speaking/evaluate-by-question",
                    data={
                        "questionId": speaking_question["id"],
                        "userExamPartId": user_exam_part_id,
                    },
                    files={"file": (args.audio.name, audio_handle, "audio/mpeg")},
                    timeout=240,
                )
            )["result"]
        speaking_band = speaking.get("overall_band")
        if not isinstance(speaking_band, (int, float)) or not 0 <= speaking_band <= 9:
            raise AssertionError(f"Invalid Speaking band: {speaking}")
        if not speaking.get("examiner_feedback"):
            raise AssertionError(f"Missing Speaking feedback: {speaking}")
        print(
            f"SPEAKING PASS: overall_band={speaking_band} "
            f"model_feedback={bool(speaking.get('examiner_feedback'))}"
        )


if __name__ == "__main__":
    main()
