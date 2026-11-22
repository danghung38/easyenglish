#!/usr/bin/env python3
"""Update Reading/Listening explanations without replacing exams or attempts."""

from __future__ import annotations

import argparse
from dataclasses import dataclass
from pathlib import Path

from build_test_data_seed import (
    all_answers,
    answer_explanation,
    clean,
    discover,
    groups,
    make_options,
    read_json,
    variants,
)
from load_mysql_seed import connect


SEED_USER = "dataset-seed"


@dataclass(frozen=True)
class ExplanationRecord:
    exam_title: str
    skill: str
    content: str
    explanation: str


def canonical_content(value: str) -> str:
    """Normalize the legacy section dash damaged by an older seed encoding."""
    return " ".join(
        value.replace("â€”", "-").replace("—", "-").replace("�", "-").split()
    )


def reading_records(path: Path, mock_number: int) -> list[ExplanationRecord]:
    passages = read_json(path).get("passages") or []
    answer_pool = all_answers(passages)
    records: list[ExplanationRecord] = []
    title = f"[IELTS Academic Full Mock] Test {mock_number:02d}"

    for passage in passages:
        passage_number = passage.get("passage_number", len(records) + 1)
        passage_text = clean(passage.get("content"))
        previous_text = ""
        for group in groups(passage):
            instructions = clean(group.get("instructions"))
            question_type = clean(group.get("question_type"))
            for question in group.get("questions") or []:
                if clean(question.get("text")):
                    previous_text = clean(question.get("text"))
                for question_text, answer in variants(question, previous_text):
                    if len(records) >= 40:
                        return records
                    marker = (
                        f"IELTS-AI:mock{mock_number}:reading:"
                        f"{question.get('id', len(records) + 1)}"
                    )
                    _, correct = make_options(
                        question, group, answer, answer_pool, 4, marker
                    )
                    records.append(
                        ExplanationRecord(
                            exam_title=title,
                            skill="READING",
                            content=f"{instructions}\n\n{question_text}".strip(),
                            explanation=answer_explanation(
                                skill="READING",
                                source=passage_text,
                                question=question_text,
                                answer=correct,
                                question_type=question_type,
                            ),
                        )
                    )

    if len(records) < 40 and passages:
        passage_titles = [clean(passage.get("title")) for passage in passages]
        correct = passage_titles[0] or "Passage 1"
        question_text = "Which title best describes the main subject of Passage 1?"
        content = f"Choose the correct answer.\n\n{question_text}"
        while len(records) < 40:
            records.append(
                ExplanationRecord(
                    exam_title=title,
                    skill="READING",
                    content=content,
                    explanation=answer_explanation(
                        skill="READING",
                        source=clean(passages[0].get("content")),
                        question=question_text,
                        answer=correct,
                        question_type="multiple-choice",
                    ),
                )
            )
    return records


def listening_records(path: Path, mock_number: int) -> list[ExplanationRecord]:
    sections = read_json(path).get("sections") or []
    answer_pool = all_answers(sections)
    records: list[ExplanationRecord] = []
    title = f"[IELTS Academic Full Mock] Test {mock_number:02d}"

    for section in sections:
        section_number = int(section.get("section_number", 1))
        section_title = clean(section.get("title"))
        transcript = clean(section.get("transcript"))
        previous_text = ""
        for group in groups(section):
            instructions = clean(group.get("instructions"))
            question_type = clean(group.get("question_type"))
            for question in group.get("questions") or []:
                if clean(question.get("text")):
                    previous_text = clean(question.get("text"))
                for question_text, answer in variants(question, previous_text):
                    marker = (
                        f"IELTS-AI:mock{mock_number}:listening:"
                        f"{question.get('id', len(records) + 1)}"
                    )
                    _, correct = make_options(
                        question, group, answer, answer_pool, 3, marker
                    )
                    content = (
                        f"Section {section_number} â€” {section_title}\n"
                        f"{instructions}\n\n{question_text}"
                    ).strip()
                    records.append(
                        ExplanationRecord(
                            exam_title=title,
                            skill="LISTENING",
                            content=content,
                            explanation=answer_explanation(
                                skill="LISTENING",
                                source=transcript,
                                question=question_text,
                                answer=correct,
                                question_type=question_type,
                            ),
                        )
                    )
    return records


def expected_records(dataset_root: Path) -> list[ExplanationRecord]:
    reading_files = discover(dataset_root, "reading")
    listening_files = discover(dataset_root, "listening")
    if len(reading_files) != 5 or not listening_files:
        raise ValueError(
            f"Expected 5 Reading sources and at least 1 Listening source; "
            f"found reading={len(reading_files)} listening={len(listening_files)}"
        )

    result: list[ExplanationRecord] = []
    for mock_number, reading_path in enumerate(reading_files, 1):
        listening_path = listening_files[(mock_number - 1) % len(listening_files)]
        result.extend(reading_records(reading_path, mock_number))
        result.extend(listening_records(listening_path, mock_number))
    return result


def update_database(
    *, config_path: Path, records: list[ExplanationRecord], check_only: bool
) -> None:
    expected_count = 5 * (40 + 40)
    if len(records) != expected_count:
        raise ValueError(
            f"Expected {expected_count} explanations for five mocks, found {len(records)}"
        )

    expected: dict[tuple[str, str], list[ExplanationRecord]] = {}
    for record in records:
        expected.setdefault((record.exam_title, record.skill), []).append(record)

    connection = connect(config_path)
    try:
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT q.id, e.title, ep.skill_type, q.content, q.`explain` "
                "FROM questions q "
                "JOIN exam_parts ep ON ep.id = q.exam_part_id "
                "JOIN exams e ON e.id = ep.exam_id "
                "WHERE e.created_by = %s AND ep.skill_type IN ('READING','LISTENING') "
                "AND q.is_section = 0 ORDER BY e.title, ep.skill_type, q.id",
                (SEED_USER,),
            )
            database_rows = cursor.fetchall()

            current: dict[
                tuple[str, str], list[tuple[int, str, str | None]]
            ] = {}
            for question_id, title, skill, content, explanation in database_rows:
                current.setdefault((title, skill), []).append(
                    (question_id, content, explanation)
                )

            if set(expected) != set(current):
                raise ValueError(
                    "Question bank exam/skill groups do not match the five-mock sources: "
                    f"expected={sorted(expected)} current={sorted(current)}"
                )

            changes: list[tuple[str, int]] = []
            for key, expected_group in expected.items():
                current_group = current[key]
                if len(expected_group) != 40 or len(current_group) != 40:
                    raise ValueError(
                        f"Expected exactly 40 questions for {key}; "
                        f"generated={len(expected_group)} database={len(current_group)}"
                    )
                for position, (record, database_row) in enumerate(
                    zip(expected_group, current_group, strict=True), 1
                ):
                    question_id, content, old_explanation = database_row
                    if canonical_content(record.content) != canonical_content(content):
                        raise ValueError(
                            f"Question order/content mismatch for {key} at position "
                            f"{position}: generated={record.content[:80]!r} "
                            f"database={content[:80]!r}"
                        )
                    if old_explanation != record.explanation:
                        changes.append((record.explanation, question_id))
            if not check_only:
                cursor.executemany(
                    "UPDATE questions SET `explain` = %s WHERE id = %s",
                    changes,
                )
        if check_only:
            connection.rollback()
        else:
            connection.commit()
        print(
            f"validated={len(records)} would_update={len(changes)} "
            f"mode={'check-only' if check_only else 'updated'}"
        )
    except Exception:
        connection.rollback()
        raise
    finally:
        connection.close()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", type=Path, required=True)
    parser.add_argument("--ielts-data-dir", type=Path, required=True)
    parser.add_argument("--check-only", action="store_true")
    args = parser.parse_args()
    update_database(
        config_path=args.config,
        records=expected_records(args.ielts_data_dir),
        check_only=args.check_only,
    )


if __name__ == "__main__":
    main()
