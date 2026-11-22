#!/usr/bin/env python3
"""Build an idempotent MySQL seed containing complete IELTS-style mock tests.

Reading, listening and writing material is imported from the CC BY 4.0
``LuchoBazz/ielts-ai-dataset`` repository. Speaking prompt sets are original
practice material included below. The generated content is independent IELTS-
style practice material; it is not an official IELTS/Cambridge test.

The seed replaces rows previously created by ``dataset-seed`` so the app does
not mix the former OneStopQA data with IELTS mocks. User attempts that reference
those generated exams are test data and are removed in the same transaction.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import random
import re
import shutil
from pathlib import Path
from typing import Any, Iterable


SOURCE_NAME = "LuchoBazz/ielts-ai-dataset (CC BY 4.0)"
SEED_USER = "dataset-seed"

SPEAKING_MOCKS = [
    {
        "title": "Work, a useful object, and technology",
        "part1": [
            "Do you work or are you a student?",
            "What part of your work or studies do you enjoy most?",
            "How do you usually organise your working day?",
        ],
        "part2": (
            "Describe a useful object in your home. You should say what it is, "
            "when you got it, how you use it, and explain why it is useful to you."
        ),
        "part3": [
            "Why do people replace useful objects before they stop working?",
            "How has technology changed the products people use at home?",
            "Should manufacturers be responsible for making products last longer?",
        ],
    },
    {
        "title": "Hometown, a memorable journey, and transport",
        "part1": [
            "What do you like most about your hometown?",
            "Has your hometown changed much in recent years?",
            "Would you like to live there in the future?",
        ],
        "part2": (
            "Describe a memorable journey you made. You should say where you went, "
            "who you travelled with, what happened during the journey, and explain "
            "why you remember it well."
        ),
        "part3": [
            "What makes public transport attractive to commuters?",
            "How might travel in cities change over the next twenty years?",
            "Should governments restrict private cars in city centres?",
        ],
    },
    {
        "title": "Reading, an inspiring person, and education",
        "part1": [
            "How often do you read for pleasure?",
            "What kinds of books or articles do you prefer?",
            "Did you read more when you were younger?",
        ],
        "part2": (
            "Describe a person who inspired you to learn something new. You should "
            "say who the person was, what you learned, how they helped you, and "
            "explain why this experience was important."
        ),
        "part3": [
            "What qualities make someone an effective teacher?",
            "Is learning online as effective as learning in a classroom?",
            "How should schools prepare students for jobs that do not yet exist?",
        ],
    },
    {
        "title": "Food, a public event, and local culture",
        "part1": [
            "Do you enjoy cooking?",
            "What meal is popular where you live?",
            "Have your eating habits changed in recent years?",
        ],
        "part2": (
            "Describe a public event you enjoyed attending. You should say what the "
            "event was, where and when it took place, who you went with, and explain "
            "what made it enjoyable."
        ),
        "part3": [
            "Why are public celebrations important to communities?",
            "Can tourism help preserve local traditions?",
            "How can traditional culture remain relevant to young people?",
        ],
    },
    {
        "title": "Nature, a difficult decision, and the environment",
        "part1": [
            "How often do you spend time outdoors?",
            "What natural places are available near your home?",
            "What outdoor activity would you like to try?",
        ],
        "part2": (
            "Describe a difficult decision that had a good result. You should say "
            "what the decision was, why it was difficult, who helped you, and "
            "explain why the result was positive."
        ),
        "part3": [
            "Why do environmental decisions often involve difficult trade-offs?",
            "Should individuals or governments carry more responsibility for pollution?",
            "How can cities encourage people to live more sustainably?",
        ],
    },
]


def sql(value: object | None) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (int, float)):
        return str(value)
    text = str(value).replace("\\", "\\\\").replace("'", "''")
    return "'" + text + "'"


def clean(value: object | None) -> str:
    if value is None:
        return ""
    text = str(value)
    if "Â" in text or "â" in text:
        try:
            text = text.encode("latin-1").decode("utf-8")
        except (UnicodeEncodeError, UnicodeDecodeError):
            pass
    return re.sub(r"\s+", " ", text).strip()


EXPLANATION_STOP_WORDS = {
    "a", "an", "and", "are", "as", "at", "be", "been", "being", "by",
    "did", "do", "does", "for", "from", "had", "has", "have", "how",
    "in", "into", "is", "it", "its", "of", "on", "or", "that", "the",
    "their", "there", "these", "they", "this", "to", "was", "were",
    "what", "when", "where", "which", "who", "why", "with", "would",
}


def _meaningful_words(value: str) -> set[str]:
    return {
        word
        for word in re.findall(r"[a-z0-9]+", value.casefold())
        if len(word) > 2 and word not in EXPLANATION_STOP_WORDS
    }


def _source_segments(source: str) -> list[str]:
    """Split a passage/transcript into short, readable evidence candidates."""
    normalized = clean(source)
    if not normalized:
        return []
    chunks = re.split(
        r"(?<=[.!?])\s+(?=(?:\([A-Z]\)\s*)?[A-Z])|"
        r"(?=\b(?:Tutor|Professor|Interviewer|Speaker|Man|Woman|Liam|Chloe):)",
        normalized,
    )
    return [chunk.strip() for chunk in chunks if chunk.strip()]


def _paragraph_evidence(source: str, paragraph: str) -> str | None:
    match = re.search(
        rf"\({re.escape(paragraph.upper())}\)\s*(.*?)(?=\s*\([A-Z]\)\s|$)",
        clean(source),
        re.DOTALL,
    )
    if not match:
        return None
    segments = _source_segments(match.group(1))
    return segments[0] if segments else clean(match.group(1))


def find_evidence(source: str, question: str, answer: str) -> str:
    """Return the source sentence most closely supporting a correct answer."""
    paragraph_match = re.fullmatch(r"[A-Z]", answer.strip(), re.IGNORECASE)
    if paragraph_match:
        paragraph = _paragraph_evidence(source, answer.strip())
        if paragraph:
            return paragraph[:360]

    question_paragraph = re.search(
        r"\bparagraph\s+([A-Z])\b", question, re.IGNORECASE
    )
    candidates = _source_segments(source)
    if question_paragraph:
        paragraph_text = _paragraph_evidence(source, question_paragraph.group(1))
        if paragraph_text:
            candidates = _source_segments(paragraph_text) or [paragraph_text]
    if not candidates:
        return ""

    answer_phrase = answer.casefold().strip()
    generic_answers = {"true", "false", "yes", "no", "not given"}
    answer_words = (
        set() if answer_phrase in generic_answers else _meaningful_words(answer)
    )
    question_words = _meaningful_words(question)

    def score(segment: str) -> tuple[int, int]:
        segment_folded = segment.casefold()
        segment_words = _meaningful_words(segment)
        exact_bonus = (
            12
            if answer_phrase not in generic_answers
            and len(answer_phrase) > 2
            and answer_phrase in segment_folded
            else 0
        )
        answer_overlap = len(answer_words & segment_words) * 4
        question_overlap = len(question_words & segment_words)
        return exact_bonus + answer_overlap + question_overlap, -len(segment)

    return max(candidates, key=score)[:360]


def answer_explanation(
    *, skill: str, source: str, question: str, answer: str, question_type: str,
) -> str:
    """Build a learner-facing Vietnamese explanation grounded in source text."""
    evidence = find_evidence(source, question, answer)
    answer_label = answer.strip()
    normalized_type = question_type.casefold()
    prefix = "Trong bài đọc" if skill == "READING" else "Trong audio/transcript"
    quoted = f'{prefix} có bằng chứng: “{evidence}”' if evidence else prefix

    if answer_label.upper() in {"NOT GIVEN", "NOT GIVEN."}:
        return (
            f"Đáp án đúng: {answer_label}. {quoted}, nhưng nguồn không xác nhận "
            "cũng không phủ định đầy đủ mệnh đề trong câu hỏi. Vì vậy không thể "
            "kết luận TRUE/FALSE từ thông tin đã cho."
        )
    if answer_label.upper() in {"FALSE", "NO"}:
        return (
            f"Đáp án đúng: {answer_label}. {quoted}. Chi tiết này trái với mệnh "
            "đề trong câu hỏi, nên chọn đáp án phủ định."
        )
    if answer_label.upper() in {"TRUE", "YES"}:
        return (
            f"Đáp án đúng: {answer_label}. {quoted}. Chi tiết này trực tiếp xác "
            "nhận mệnh đề trong câu hỏi."
        )
    if "matching-heading" in normalized_type:
        return (
            f"Đáp án đúng: {answer_label}. {quoted}. Đây là ý chính bao quát của "
            "đoạn, còn các lựa chọn khác chỉ không khớp hoặc nói về chi tiết phụ."
        )
    return (
        f"Đáp án đúng: {answer_label}. {quoted}. Từ khóa và ý nghĩa của chi tiết "
        "này khớp với yêu cầu của câu hỏi."
    )


def slug(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def read_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8-sig"))


def cleanup_previous_seed(lines: list[str]) -> None:
    lines.extend(
        [
            "CREATE TEMPORARY TABLE seed_exam_ids (id BIGINT PRIMARY KEY);",
            f"INSERT INTO seed_exam_ids SELECT id FROM exams WHERE created_by = {sql(SEED_USER)};",
            "DELETE ua FROM user_answers ua JOIN user_exam_parts uep ON uep.id = ua.user_exam_part_id JOIN user_exams ue ON ue.id = uep.user_exam_id JOIN seed_exam_ids s ON s.id = ue.exam_id;",
            "DELETE uep FROM user_exam_parts uep JOIN user_exams ue ON ue.id = uep.user_exam_id JOIN seed_exam_ids s ON s.id = ue.exam_id;",
            "DELETE ue FROM user_exams ue JOIN seed_exam_ids s ON s.id = ue.exam_id;",
            "UPDATE questions q JOIN exam_parts ep ON ep.id = q.exam_part_id JOIN seed_exam_ids s ON s.id = ep.exam_id SET q.correct_option_id = NULL;",
            "DELETE o FROM options o JOIN questions q ON q.id = o.question_id JOIN exam_parts ep ON ep.id = q.exam_part_id JOIN seed_exam_ids s ON s.id = ep.exam_id;",
            "DELETE q FROM questions q JOIN exam_parts ep ON ep.id = q.exam_part_id JOIN seed_exam_ids s ON s.id = ep.exam_id;",
            "DELETE ep FROM exam_parts ep JOIN seed_exam_ids s ON s.id = ep.exam_id;",
            "DELETE e FROM exams e JOIN seed_exam_ids s ON s.id = e.id;",
            "DROP TEMPORARY TABLE seed_exam_ids;",
        ]
    )


def emit_exam(lines: list[str], number: int, source_titles: list[str]) -> None:
    title = f"[IELTS Academic Full Mock] Test {number:02d}"
    description = (
        "Complete IELTS-style Academic practice test with Listening, Reading, "
        "Writing and Speaking. Independent synthetic practice material; not an "
        "official IELTS/Cambridge paper. Sources: " + "; ".join(source_titles)
    )
    lines.extend(
        [
            f"SET @seed_title = {sql(title)};",
            "INSERT INTO exams (title, description, total_duration, image_url, created_by, updated_by)",
            f"VALUES (@seed_title, {sql(description)}, 165, NULL, {sql(SEED_USER)}, {sql(SEED_USER)});",
            "SET @exam_id = LAST_INSERT_ID();",
        ]
    )


def emit_part(lines: list[str], skill: str, duration: int) -> None:
    lines.extend(
        [
            "INSERT INTO exam_parts (skill_type, duration, exam_id, created_by, updated_by)",
            f"VALUES ({sql(skill)}, {duration}, @exam_id, {sql(SEED_USER)}, {sql(SEED_USER)});",
            "SET @part_id = LAST_INSERT_ID();",
        ]
    )


def emit_question(
    lines: list[str], *, skill: str, section: str, content: str,
    marker: str, options: list[str] | None = None,
    correct: str | None = None, audio_url: str | None = None,
    image_url: str | None = None, transcript: str | None = None,
    max_score: float = 1.0, explanation_text: str | None = None,
) -> None:
    explanation = (
        explanation_text
        or (f"{marker} | Correct answer: {correct}" if correct else marker)
    )
    lines.extend(
        [
            "INSERT INTO questions",
            "(skill_type, is_section, content, audio_url, image_url, section, `explain`, transcript, correct_option_id, max_score, exam_part_id, created_by, updated_by)",
            f"VALUES ({sql(skill)}, 0, {sql(content)}, {sql(audio_url)}, {sql(image_url)}, {sql(section)}, {sql(explanation)}, {sql(transcript)}, NULL, {max_score}, @part_id, {sql(SEED_USER)}, {sql(SEED_USER)});",
            "SET @question_id = LAST_INSERT_ID();",
            "SET @correct_id = NULL;",
        ]
    )
    if options and correct:
        for option in options:
            lines.extend(
                [
                    "INSERT INTO options (content, question_id, created_by, updated_by)",
                    f"VALUES ({sql(option)}, @question_id, {sql(SEED_USER)}, {sql(SEED_USER)});",
                    f"SET @correct_id = IF({sql(option)} = {sql(correct)}, LAST_INSERT_ID(), @correct_id);",
                ]
            )
        lines.append("UPDATE questions SET correct_option_id = @correct_id WHERE id = @question_id;")


def emit_section(
    lines: list[str], *, skill: str, section: str, content: str,
    marker: str,
) -> None:
    """Emit a display-only section row that is not part of the scored items."""
    lines.extend(
        [
            "INSERT INTO questions",
            "(skill_type, is_section, content, audio_url, image_url, section, `explain`, transcript, correct_option_id, max_score, exam_part_id, created_by, updated_by)",
            f"VALUES ({sql(skill)}, 1, {sql(content)}, NULL, NULL, {sql(section)}, {sql(marker)}, NULL, NULL, 0, @part_id, {sql(SEED_USER)}, {sql(SEED_USER)});",
        ]
    )


def groups(unit: dict[str, Any]) -> list[dict[str, Any]]:
    return unit.get("question_groups") or unit.get("groups") or []


def variants(question: dict[str, Any], previous_text: str) -> Iterable[tuple[str, str]]:
    text = clean(question.get("text")) or previous_text
    answer = clean(question.get("answer"))
    if answer:
        yield text, answer
        return
    for gap in question.get("completion_gaps") or []:
        gap_answer = clean(gap.get("answer"))
        if gap_answer:
            yield clean(gap.get("gap_text")) or text, gap_answer


def option_text(value: Any) -> tuple[str, str]:
    if isinstance(value, dict):
        return clean(value.get("id")), clean(value.get("text"))
    text = clean(value)
    match = re.match(r"^([A-Z]|[ivxlcdm]+)[.)]\s*(.+)$", text, re.IGNORECASE)
    return (match.group(1), match.group(2)) if match else (text, text)


def make_options(
    question: dict[str, Any], group: dict[str, Any], answer: str,
    answer_pool: list[str], limit: int, marker: str,
) -> tuple[list[str], str]:
    question_type = clean(group.get("question_type")).casefold()
    if question_type == "true-false-not-given":
        return ["TRUE", "FALSE", "NOT GIVEN"], answer.strip().upper()
    if question_type == "yes-no-not-given":
        return ["YES", "NO", "NOT GIVEN"], answer.strip().upper()

    candidates: list[tuple[str, str]] = []
    for value in question.get("options") or []:
        candidates.append(option_text(value))
    for value in group.get("word_bank") or []:
        candidates.append(option_text(value))
    for pair in question.get("matching_pairs") or []:
        right = clean(pair.get("right"))
        if right:
            candidates.append((right, right))

    answer_key = answer.strip()
    correct = ""
    for key, text in candidates:
        if key.casefold() == answer_key.casefold() or text.casefold() == answer_key.casefold():
            correct = text
            break
    if not correct:
        correct = answer_key

    texts: list[str] = []
    for _, text in candidates:
        if text and text.casefold() not in {item.casefold() for item in texts}:
            texts.append(text)
    if correct.casefold() not in {item.casefold() for item in texts}:
        texts.append(correct)
    for distractor in answer_pool:
        if distractor and distractor.casefold() not in {item.casefold() for item in texts}:
            texts.append(distractor)
        if len(texts) >= max(limit * 2, limit + 1):
            break
    for fallback in ("NOT GIVEN", "None of the above", "Insufficient information"):
        if fallback.casefold() not in {item.casefold() for item in texts}:
            texts.append(fallback)

    distractors = [item for item in texts if item.casefold() != correct.casefold()]
    rng = random.Random(int(hashlib.sha256(marker.encode("utf-8")).hexdigest()[:16], 16))
    rng.shuffle(distractors)
    selected = [correct] + distractors[: limit - 1]
    rng.shuffle(selected)
    return selected, correct


def all_answers(units: list[dict[str, Any]]) -> list[str]:
    result: list[str] = []
    for unit in units:
        for group in groups(unit):
            for question in group.get("questions") or []:
                answer = clean(question.get("answer"))
                if answer:
                    result.append(answer)
                for gap in question.get("completion_gaps") or []:
                    answer = clean(gap.get("answer"))
                    if answer:
                        result.append(answer)
    return result


def emit_reading(lines: list[str], path: Path, mock_number: int) -> int:
    data = read_json(path)
    passages = data.get("passages") or []
    answer_pool = all_answers(passages)
    emit_part(lines, "READING", 60)
    count = 0
    for passage in passages:
        passage_number = passage.get("passage_number", count + 1)
        passage_title = clean(passage.get("title"))
        passage_text = clean(passage.get("content"))
        section = f"Passage {passage_number}"
        emit_section(
            lines,
            skill="READING",
            section=section,
            content=f"PASSAGE {passage_number}: {passage_title}\n\n{passage_text}",
            marker=f"IELTS-AI:mock{mock_number}:reading:passage:{passage_number}",
        )
        previous_text = ""
        for group in groups(passage):
            instructions = clean(group.get("instructions"))
            for question in group.get("questions") or []:
                if clean(question.get("text")):
                    previous_text = clean(question.get("text"))
                for question_text, answer in variants(question, previous_text):
                    # A real IELTS Academic Reading paper contains exactly 40
                    # scored items. Some upstream files contain 39 or 41
                    # answered gaps because a table row is represented as one
                    # structural question; cap the exported paper at 40.
                    if count >= 40:
                        return count
                    marker = f"IELTS-AI:mock{mock_number}:reading:{question.get('id', count + 1)}"
                    opts, correct = make_options(question, group, answer, answer_pool, 4, marker)
                    content = f"{instructions}\n\n{question_text}".strip()
                    emit_question(
                        lines, skill="READING", section=section,
                        content=content, marker=marker, options=opts, correct=correct,
                        explanation_text=answer_explanation(
                            skill="READING",
                            source=passage_text,
                            question=question_text,
                            answer=correct,
                            question_type=clean(group.get("question_type")),
                        ),
                    )
                    count += 1

    # Fill the three 39-item source files with one deterministic, passage-based
    # multiple-choice item so every exported mock keeps the official 40-item
    # structure without inventing an answer unrelated to its passage.
    if count < 40 and passages:
        titles = [clean(passage.get("title")) for passage in passages]
        correct = titles[0] or "Passage 1"
        options = [title for title in titles if title]
        if correct not in options:
            options.insert(0, correct)
        while len(options) < 4:
            options.append(f"None of the listed passage topics {len(options) + 1}")
        while count < 40:
            marker = f"IELTS-SYNTHETIC:mock{mock_number}:reading:supplemental:{count + 1}"
            emit_question(
                lines,
                skill="READING",
                section="Passage 1",
                content=(
                    "Choose the correct answer.\n\n"
                    "Which title best describes the main subject of Passage 1?"
                ),
                marker=marker,
                options=options[:4],
                correct=correct,
                explanation_text=answer_explanation(
                    skill="READING",
                    source=clean(passages[0].get("content")),
                    question="Which title best describes the main subject of Passage 1?",
                    answer=correct,
                    question_type="multiple-choice",
                ),
            )
            count += 1
    return count


def copy_listening_media(manifest_path: Path, media_dir: Path) -> tuple[str, dict[int, str]]:
    test_slug = slug(manifest_path.parent.name)
    destination = media_dir / "audio" / test_slug
    destination.mkdir(parents=True, exist_ok=True)
    names: dict[int, str] = {}
    for audio in sorted(manifest_path.parent.glob("*.mp3")):
        match = re.search(r"section-(\d+)", audio.stem)
        if not match:
            continue
        section_number = int(match.group(1))
        shutil.copy2(audio, destination / audio.name)
        names[section_number] = audio.name
    return test_slug, names


def emit_listening(
    lines: list[str], path: Path, mock_number: int,
    media_dir: Path, media_base_url: str,
) -> int:
    data = read_json(path)
    sections = data.get("sections") or []
    answer_pool = all_answers(sections)
    test_slug, audio_names = copy_listening_media(path, media_dir)
    emit_part(lines, "LISTENING", 30)
    count = 0
    for section in sections:
        section_number = int(section.get("section_number", 1))
        section_title = clean(section.get("title"))
        transcript = clean(section.get("transcript"))
        audio_name = audio_names.get(section_number)
        audio_url = (
            f"{media_base_url.rstrip('/')}/{test_slug}/{audio_name}"
            if audio_name else None
        )
        previous_text = ""
        for group in groups(section):
            instructions = clean(group.get("instructions"))
            for question in group.get("questions") or []:
                if clean(question.get("text")):
                    previous_text = clean(question.get("text"))
                for question_text, answer in variants(question, previous_text):
                    marker = f"IELTS-AI:mock{mock_number}:listening:{question.get('id', count + 1)}"
                    opts, correct = make_options(question, group, answer, answer_pool, 3, marker)
                    content = f"Section {section_number} — {section_title}\n{instructions}\n\n{question_text}".strip()
                    emit_question(
                        lines, skill="LISTENING", section=f"Section {section_number}",
                        content=content, marker=marker, options=opts, correct=correct,
                        audio_url=audio_url, transcript=transcript,
                        explanation_text=answer_explanation(
                            skill="LISTENING",
                            source=transcript,
                            question=question_text,
                            answer=correct,
                            question_type=clean(group.get("question_type")),
                        ),
                    )
                    count += 1
    return count


def copy_writing_image(path: Path, media_dir: Path) -> str | None:
    for suffix in (".jpeg", ".jpg", ".png", ".webp"):
        candidate = path.with_suffix(suffix)
        if candidate.exists():
            destination = media_dir / "images"
            destination.mkdir(parents=True, exist_ok=True)
            shutil.copy2(candidate, destination / candidate.name)
            return candidate.name
    return None


def emit_writing(
    lines: list[str], path: Path, mock_number: int, media_dir: Path,
    image_base_url: str,
) -> int:
    data = read_json(path)
    image_name = copy_writing_image(path, media_dir)
    image_url = f"{image_base_url.rstrip('/')}/{image_name}" if image_name else None
    emit_part(lines, "WRITING", 60)
    count = 0
    for task in data.get("tasks") or []:
        task_number = int(task.get("task_number", count + 1))
        figure = clean(task.get("figure_description"))
        content = (
            f"{clean(task.get('title'))}\n\n{clean(task.get('prompt'))}\n\n"
            f"Minimum words: {task.get('min_words', 150 if task_number == 1 else 250)}"
        )
        if figure:
            content += f"\n\nFigure description: {figure}"
        emit_question(
            lines, skill="WRITING", section=f"task{task_number}", content=content,
            marker=f"IELTS-AI:mock{mock_number}:writing:{task.get('id', count + 1)}",
            image_url=image_url if task_number == 1 else None, max_score=9.0,
        )
        count += 1
    return count


def emit_speaking(lines: list[str], mock_number: int, prompt: dict[str, Any]) -> int:
    """Emit distinct Part 1, Part 2 and Part 3 rows for one demo mock."""
    emit_part(lines, "SPEAKING", 15)
    count = 0

    for number, content in enumerate(prompt["part1"], 1):
        emit_question(
            lines,
            skill="SPEAKING",
            section="part1",
            content=content,
            marker=f"IELTS-SYNTHETIC:mock{mock_number}:speaking:part1:{number}",
            max_score=9.0,
        )
        count += 1

    emit_question(
        lines,
        skill="SPEAKING",
        section="part2",
        content=prompt["part2"],
        marker=f"IELTS-SYNTHETIC:mock{mock_number}:speaking:part2:1",
        max_score=9.0,
    )
    count += 1

    for number, content in enumerate(prompt["part3"], 1):
        emit_question(
            lines,
            skill="SPEAKING",
            section="part3",
            content=content,
            marker=f"IELTS-SYNTHETIC:mock{mock_number}:speaking:part3:{number}",
            max_score=9.0,
        )
        count += 1

    return count


def discover(root: Path, kind: str) -> list[Path]:
    if kind == "listening":
        complete: list[Path] = []
        for manifest in sorted(root.glob("datasets/**/listening/**/manifest.json")):
            data = read_json(manifest)
            section_numbers = {
                int(section.get("section_number", 1))
                for section in data.get("sections") or []
            }
            audio_numbers = {
                int(match.group(1))
                for audio in manifest.parent.glob("section-*.mp3")
                if (match := re.search(r"section-(\d+)", audio.stem))
            }
            if section_numbers and section_numbers <= audio_numbers:
                complete.append(manifest)
        return complete
    return sorted(root.glob(f"datasets/**/{kind}/*.json"))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--ielts-data-dir", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--media-dir", type=Path, required=True)
    parser.add_argument(
        "--media-base-url",
        default="http://10.0.2.2:8081/api/ielts/audio",
        help="URL used by the Android emulator for locally served MP3 files.",
    )
    parser.add_argument(
        "--image-base-url",
        default="http://10.0.2.2:8081/api/ielts/images",
    )
    args = parser.parse_args()

    reading_files = discover(args.ielts_data_dir, "reading")
    listening_files = discover(args.ielts_data_dir, "listening")
    writing_files = discover(args.ielts_data_dir, "writing")
    if not reading_files or not listening_files or not writing_files:
        raise SystemExit("IELTS dataset is incomplete: reading, listening and writing are required")

    lines = [
        "-- Generated IELTS-style test data. Re-running replaces only dataset-seed rows.",
        f"-- Attribution: {SOURCE_NAME}",
        "SET NAMES utf8mb4;",
        "SET collation_connection = utf8mb4_unicode_ci;",
        "SET FOREIGN_KEY_CHECKS = 1;",
        "START TRANSACTION;",
    ]
    cleanup_previous_seed(lines)

    totals = {"reading": 0, "listening": 0, "writing": 0, "speaking": 0}
    for index, reading_path in enumerate(reading_files, 1):
        listening_path = listening_files[(index - 1) % len(listening_files)]
        writing_path = writing_files[(index - 1) % len(writing_files)]
        speaking_prompt = SPEAKING_MOCKS[(index - 1) % len(SPEAKING_MOCKS)]
        emit_exam(
            lines,
            index,
            [
                SOURCE_NAME,
                f"Reading: {reading_path.stem}",
                f"Listening: {listening_path.parent.name}",
                f"Writing: {writing_path.stem}",
                "Speaking: original synthetic prompt bank",
            ],
        )
        totals["listening"] += emit_listening(
            lines, listening_path, index, args.media_dir, args.media_base_url
        )
        totals["reading"] += emit_reading(lines, reading_path, index)
        totals["writing"] += emit_writing(
            lines, writing_path, index, args.media_dir, args.image_base_url
        )
        totals["speaking"] += emit_speaking(lines, index, speaking_prompt)

    lines.append("COMMIT;")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(
        f"Generated {len(reading_files)} complete IELTS mocks: "
        f"{totals['listening']} listening, {totals['reading']} reading, "
        f"{totals['writing']} writing, {totals['speaking']} speaking questions."
    )


if __name__ == "__main__":
    main()
