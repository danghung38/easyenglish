#!/usr/bin/env python3
"""Load a generated SQL seed into the local MySQL database.

This helper reads the datasource settings from ``application.yaml`` and keeps
credentials out of command-line arguments. It requires PyMySQL in the active
Python environment.
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

import pymysql
from pymysql.constants import CLIENT


def datasource(config_path: Path) -> tuple[str, int, str, str, str]:
    config = config_path.read_text(encoding="utf-8")
    url_match = re.search(
        r'url:\s*["\']?jdbc:mysql://([^/:"\']+)(?::(\d+))?/([^?"\']+)', config
    )
    user_match = re.search(r"^\s*username:\s*([^\s#]+)", config, re.MULTILINE)
    password_match = re.search(r"^\s*password:\s*([^\s#]+)", config, re.MULTILINE)
    if not (url_match and user_match and password_match):
        raise ValueError(f"Cannot read datasource settings from {config_path}")
    host, port, database = url_match.groups()
    username = user_match.group(1)
    password = password_match.group(1)

    # Match Spring's optional:file:./.env.backend override used by this project.
    env_path = config_path.resolve().parents[3] / ".env.backend"
    if env_path.exists():
        overrides: dict[str, str] = {}
        for raw_line in env_path.read_text(encoding="utf-8-sig").splitlines():
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            overrides[key.strip()] = value.strip().strip('"').strip("'")
        username = overrides.get("spring.datasource.username", username)
        password = overrides.get("spring.datasource.password", password)
    return host, int(port or 3306), database, username, password


def connect(config_path: Path) -> pymysql.Connection:
    host, port, database, username, password = datasource(config_path)
    return pymysql.connect(
        host=host,
        port=port,
        user=username,
        password=password,
        database=database,
        charset="utf8mb4",
        client_flag=CLIENT.MULTI_STATEMENTS,
        autocommit=False,
    )


def sql_statements(script: str):
    """Yield SQL statements without splitting semicolons inside quoted text."""
    buffer: list[str] = []
    quote: str | None = None
    index = 0
    while index < len(script):
        char = script[index]
        next_char = script[index + 1] if index + 1 < len(script) else ""
        if quote:
            buffer.append(char)
            if char == "\\" and next_char:
                buffer.append(next_char)
                index += 2
                continue
            if char == quote:
                if next_char == quote:
                    buffer.append(next_char)
                    index += 2
                    continue
                quote = None
        else:
            if char in ("'", '"', "`"):
                quote = char
                buffer.append(char)
            elif char == "-" and next_char == "-":
                newline = script.find("\n", index + 2)
                index = len(script) if newline < 0 else newline
                buffer.append("\n")
            elif char == ";":
                statement = "".join(buffer).strip()
                if statement:
                    yield statement
                buffer.clear()
            else:
                buffer.append(char)
        index += 1
    statement = "".join(buffer).strip()
    if statement:
        yield statement


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", type=Path, required=True)
    parser.add_argument("--sql", type=Path, required=True)
    parser.add_argument(
        "--seed-user",
        default="dataset-seed",
        help="created_by marker used by the seed being loaded",
    )
    parser.add_argument("--check-only", action="store_true")
    args = parser.parse_args()

    connection = connect(args.config)
    try:
        with connection.cursor() as cursor:
            cursor.execute("SELECT DATABASE()")
            database = cursor.fetchone()[0]
            cursor.execute(
                "SELECT COUNT(*) FROM exams WHERE created_by = %s",
                (args.seed_user,),
            )
            old_exams = cursor.fetchone()[0]
            cursor.execute(
                "SELECT COUNT(*) FROM user_exams ue "
                "JOIN exams e ON e.id = ue.exam_id "
                "WHERE e.created_by = %s",
                (args.seed_user,),
            )
            old_attempts = cursor.fetchone()[0]
            if args.check_only:
                print(
                    f"database={database} generated_exams={old_exams} "
                    f"generated_attempts={old_attempts}"
                )
                return

            statement_count = 0
            for statement in sql_statements(args.sql.read_text(encoding="utf-8")):
                cursor.execute(statement)
                statement_count += 1
        connection.commit()
        print(
            f"Loaded {args.sql} into {database}; replaced {old_exams} generated "
            f"exams and {old_attempts} generated test attempts "
            f"using {statement_count} statements."
        )
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT ep.skill_type, COUNT(DISTINCT ep.id), COUNT(q.id), "
                "SUM(q.correct_option_id IS NOT NULL), SUM(q.audio_url IS NOT NULL) "
                "FROM exams e JOIN exam_parts ep ON ep.exam_id = e.id "
                "LEFT JOIN questions q ON q.exam_part_id = ep.id "
                "WHERE e.created_by = %s GROUP BY ep.skill_type "
                "ORDER BY ep.skill_type",
                (args.seed_user,),
            )
            for skill, parts, questions, scored, audio in cursor.fetchall():
                print(
                    f"{skill}: parts={parts} questions={questions} "
                    f"answer_keys={int(scored or 0)} audio_questions={int(audio or 0)}"
                )
    except Exception:
        try:
            connection.rollback()
        except pymysql.MySQLError:
            pass
        raise
    finally:
        connection.close()


if __name__ == "__main__":
    main()
