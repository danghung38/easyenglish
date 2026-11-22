import sys
import unittest
from collections import Counter
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
sys.path.insert(0, str(SCRIPT_DIR))

from build_test_data_seed import answer_explanation, find_evidence
from update_rl_explanations import canonical_content, expected_records


DATASET_ROOT = SCRIPT_DIR.parents[1] / "test-data" / "ielts-ai-dataset"


class ReadingListeningExplanationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.records = expected_records(DATASET_ROOT)

    def test_all_five_mocks_have_forty_explanations_per_skill(self):
        distribution = Counter(
            (record.exam_title, record.skill) for record in self.records
        )

        self.assertEqual(400, len(self.records))
        self.assertEqual(10, len(distribution))
        self.assertTrue(all(count == 40 for count in distribution.values()))

    def test_every_explanation_is_grounded_and_learner_facing(self):
        for record in self.records:
            with self.subTest(exam=record.exam_title, skill=record.skill):
                self.assertTrue(record.explanation.startswith("Đáp án đúng:"))
                self.assertIn("có bằng chứng:", record.explanation)
                self.assertNotIn("IELTS-AI:", record.explanation)
                self.assertNotIn("Correct answer:", record.explanation)

    def test_boolean_answer_does_not_match_generic_true_word(self):
        source = (
            "(A) The true nature of the object remained unknown. "
            "(B) Valerios Stais noticed a gear wheel inside the rock."
        )
        evidence = find_evidence(
            source,
            "Valerios Stais noticed a mechanical component.",
            "TRUE",
        )

        self.assertIn("Valerios Stais", evidence)

    def test_not_given_explains_missing_confirmation(self):
        explanation = answer_explanation(
            skill="READING",
            source="The passage describes hydroponic farming.",
            question="Aeroponics always uses less water than hydroponics.",
            answer="NOT GIVEN",
            question_type="true-false-not-given",
        )

        self.assertIn("không xác nhận", explanation)
        self.assertIn("không phủ định", explanation)

    def test_legacy_replacement_character_matches_section_dash(self):
        self.assertEqual(
            canonical_content("Section 1 — Topic"),
            canonical_content("Section 1 � Topic"),
        )


if __name__ == "__main__":
    unittest.main()
