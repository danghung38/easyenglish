#!/usr/bin/env python3
"""Synchronise Reading passage rows for the five generated IELTS mock tests.

The original seed embedded each passage in the first scored question of its
section. This migration creates one display-only ``is_section`` row per passage
and restores the first scored question to question-only content. It is safe to
run repeatedly and does not delete exams or learner attempts.
"""

from __future__ import annotations

import argparse
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from build_test_data_seed import clean, discover, groups, read_json, variants
from load_mysql_seed import connect


SEED_USER = "dataset-seed"
EXPECTED_MOCKS = 5
EXPECTED_PASSAGES_PER_MOCK = 3
EXPECTED_SCORED_QUESTIONS = 40


@dataclass(frozen=True)
class PassageData:
    number: int
    title: str
    text: str
    first_question: str

    @property
    def section(self) -> str:
        return f"Passage {self.number}"

    @property
    def display_content(self) -> str:
        return f"PASSAGE {self.number}: {self.title}\n\n{self.text}"


def first_question_content(passage: dict[str, Any]) -> str:
    """Rebuild the first scored question exactly as the seed generator does."""
    previous_text = ""
    for group in groups(passage):
        instructions = clean(group.get("instructions"))
        for question in group.get("questions") or []:
            if clean(question.get("text")):
                previous_text = clean(question.get("text"))
            generated = next(iter(variants(question, previous_text)), None)
            if generated:
                question_text, _ = generated
                return f"{instructions}\n\n{question_text}".strip()
    raise ValueError("Reading passage does not contain a scored question")


def load_mock_passages(dataset_root: Path) -> list[list[PassageData]]:
    reading_files = discover(dataset_root, "reading")
    if len(reading_files) != EXPECTED_MOCKS:
        raise ValueError(
            f"Expected {EXPECTED_MOCKS} Reading files, found {len(reading_files)}"
        )

    mocks: list[list[PassageData]] = []
    for path in reading_files:
        source_passages = read_json(path).get("passages") or []
        if len(source_passages) != EXPECTED_PASSAGES_PER_MOCK:
            raise ValueError(
                f"{path} must contain {EXPECTED_PASSAGES_PER_MOCK} passages"
            )
        mocks.append(
            [
                PassageData(
                    number=int(passage.get("passage_number", index)),
                    title=clean(passage.get("title")),
                    text=clean(passage.get("content")),
                    first_question=first_question_content(passage),
                )
                for index, passage in enumerate(source_passages, 1)
            ]
        )
    return mocks


def reading_part_id(cursor, mock_number: int) -> int:
    title = f"[IELTS Academic Full Mock] Test {mock_number:02d}"
    cursor.execute(
        "SELECT ep.id FROM exam_parts ep "
        "JOIN exams e ON e.id = ep.exam_id "
        "WHERE e.title = %s AND ep.skill_type = 'READING'",
        (title,),
    )
    rows = cursor.fetchall()
    if len(rows) != 1:
        raise ValueError(f"Expected one Reading part for {title}, found {len(rows)}")
    return int(rows[0][0])


def upsert_passage(cursor, part_id: int, mock_number: int, passage: PassageData) -> None:
    cursor.execute(
        "SELECT id FROM questions WHERE exam_part_id = %s AND skill_type = 'READING' "
        "AND section = %s AND is_section = 1 ORDER BY id",
        (part_id, passage.section),
    )
    section_rows = cursor.fetchall()
    marker = f"IELTS-AI:mock{mock_number}:reading:passage:{passage.number}"
    if section_rows:
        section_id = int(section_rows[0][0])
        cursor.execute(
            "UPDATE questions SET content = %s, `explain` = %s, max_score = 0, "
            "updated_by = %s, update_at = NOW() WHERE id = %s",
            (passage.display_content, marker, SEED_USER, section_id),
        )
        duplicate_ids = [int(row[0]) for row in section_rows[1:]]
        if duplicate_ids:
            placeholders = ",".join(["%s"] * len(duplicate_ids))
            cursor.execute(f"DELETE FROM questions WHERE id IN ({placeholders})", duplicate_ids)
    else:
        cursor.execute(
            "INSERT INTO questions "
            "(skill_type, is_section, content, section, `explain`, max_score, "
            "exam_part_id, created_by, updated_by, create_at, update_at) "
            "VALUES ('READING', 1, %s, %s, %s, 0, %s, %s, %s, NOW(), NOW())",
            (
                passage.display_content,
                passage.section,
                marker,
                part_id,
                SEED_USER,
                SEED_USER,
            ),
        )

    cursor.execute(
        "SELECT id FROM questions WHERE exam_part_id = %s AND skill_type = 'READING' "
        "AND section = %s AND is_section = 0 ORDER BY id LIMIT 1",
        (part_id, passage.section),
    )
    first_question = cursor.fetchone()
    if not first_question:
        raise ValueError(f"No scored question found for {passage.section} in part {part_id}")
    cursor.execute(
        "UPDATE questions SET content = %s, updated_by = %s, update_at = NOW() "
        "WHERE id = %s",
        (passage.first_question, SEED_USER, int(first_question[0])),
    )


def validate(cursor) -> list[str]:
    cursor.execute(
        "SELECT e.title, ep.id, "
        "SUM(CASE WHEN q.is_section = 1 THEN 1 ELSE 0 END), "
        "SUM(CASE WHEN q.is_section = 0 THEN 1 ELSE 0 END), "
        "SUM(CASE WHEN q.is_section = 1 AND CHAR_LENGTH(q.content) < 500 THEN 1 ELSE 0 END), "
        "SUM(CASE WHEN q.is_section = 0 AND q.content LIKE 'PASSAGE %' THEN 1 ELSE 0 END) "
        "FROM exams e JOIN exam_parts ep ON ep.exam_id = e.id "
        "JOIN questions q ON q.exam_part_id = ep.id "
        "WHERE e.title LIKE '[IELTS Academic Full Mock] Test %%' "
        "AND ep.skill_type = 'READING' "
        "GROUP BY e.title, ep.id ORDER BY e.title"
    )
    rows = cursor.fetchall()
    errors: list[str] = []
    if len(rows) != EXPECTED_MOCKS:
        errors.append(f"Expected {EXPECTED_MOCKS} Reading mocks, found {len(rows)}")
    for title, _, sections, questions, short_sections, embedded_passages in rows:
        if int(sections or 0) != EXPECTED_PASSAGES_PER_MOCK:
            errors.append(f"{title}: expected 3 passage rows, found {sections}")
        if int(questions or 0) != EXPECTED_SCORED_QUESTIONS:
            errors.append(f"{title}: expected 40 scored questions, found {questions}")
        if int(short_sections or 0):
            errors.append(f"{title}: contains {short_sections} unexpectedly short passages")
        if int(embedded_passages or 0):
            errors.append(f"{title}: passage text remains embedded in a scored question")
    return errors


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--ielts-data-dir", type=Path, required=True)
    parser.add_argument("--config", type=Path, required=True)
    parser.add_argument("--check-only", action="store_true")
    args = parser.parse_args()

    mocks = load_mock_passages(args.ielts_data_dir)
    connection = connect(args.config)
    try:
        with connection.cursor() as cursor:
            if not args.check_only:
                for mock_number, passages in enumerate(mocks, 1):
                    part_id = reading_part_id(cursor, mock_number)
                    for passage in passages:
                        upsert_passage(cursor, part_id, mock_number, passage)
            errors = validate(cursor)
            if errors:
                raise ValueError("Reading passage validation failed:\n- " + "\n- ".join(errors))
        if not args.check_only:
            connection.commit()
        print(
            f"Reading passages valid: {EXPECTED_MOCKS} mocks, "
            f"{EXPECTED_MOCKS * EXPECTED_PASSAGES_PER_MOCK} passages, "
            f"{EXPECTED_MOCKS * EXPECTED_SCORED_QUESTIONS} scored questions."
        )
    except Exception:
        connection.rollback()
        raise
    finally:
        connection.close()


if __name__ == "__main__":
    main()
