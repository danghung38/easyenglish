import React, { lazy, Suspense, useContext, useState } from "react";
import HomePage from "./pages/base/HomePage";
import LoginPage from "./pages/base/LoginPage";
import RegisterPage from "./pages/base/RegisterPage";
import VerifyEmailPage from "./pages/base/VerifyEmailPage";
import { Route, Routes } from "react-router-dom";

import PrivateRoutes from "./utils/PrivateRoutes";
import LoadingPage from "./pages/LoadingPage";
import "../src/assets/scss/_user.scss";

const DashboardPage = lazy(() => import("./pages/student/DashboardPage"));
const VocabularyPage = lazy(() => import("./pages/student/VocabularyPage"));

// Admin Pages
const AdminLayout = lazy(() => import("./pages/admin/AdminLayout"));
const AdminDashboard = lazy(() => import("./pages/admin/AdminDashboard"));
const UsersManagement = lazy(() => import("./pages/admin/UsersManagement"));
const ExamsManagement = lazy(() => import("./pages/admin/ExamsManagement"));
const ExamPartsManagement = lazy(() => import("./pages/admin/ExamPartsManagement"));
const QuestionsManagement = lazy(() => import("./pages/admin/QuestionsManagement"));
const UserExamsManagement = lazy(() => import("./pages/admin/UserExamsManagement"));
const VocabularyManagement = lazy(() => import("./pages/admin/VocabularyManagement"));
const RAGExamplesManagement = lazy(() => import("./pages/admin/RAGExamplesManagement"));
const AttemptListeningModulePage = lazy(() =>
  import("./pages/ieltstest/listening/AttemptListeningModulePage")
);
const ListeningResultPage = lazy(() =>
  import("./pages/ieltstest/listening/ListeningResultPage")
);
const ModuleHomePage = lazy(() => import("./pages/ieltstest/ModuleHomePage"));
const AttemptReadingModulePage = lazy(() =>
  import("./pages/ieltstest/reading/AttemptReadingModulePage")
);
const ReadingResultPage = lazy(() =>
  import("./pages/ieltstest/reading/ReadingResultPage")
);
const AttemptWritingModulePage = lazy(() =>
  import("./pages/ieltstest/writing/AttemptWritingModulePage")
);
const WritingResultPage = lazy(() =>
  import("./pages/ieltstest/writing/WritingResultPage")
);
const AttemptSpeakingExamPage = lazy(() =>
  import("./pages/ieltstest/speaking/AttemptSpeakingExamPage")
);
const SpeakingResultPage = lazy(() =>
  import("./pages/ieltstest/speaking/SpeakingResultPage")
);
const AccountPage = lazy(() => import("./pages/base/AccountPage"));
const FullTestInfoPage = lazy(() =>
  import("./pages/ieltstest/fulltest/FullTestInfoPage")
);
const AttemptFullTestPage = lazy(() =>
  import("./pages/ieltstest/fulltest/AttemptFullTestPage")
);
const FullTestResultPage = lazy(() =>
  import("./pages/ieltstest/fulltest/FullTestResultPage")
);
const ResetPasswordPage = lazy(() => import("./pages/base/ResetPasswordPage"));
const ConfirmResetPassword = lazy(() =>
  import("./pages/base/ConfirmResetPassword")
);
const YourTestPage = lazy(() => import("./pages/student/YourTestPage"));
const TestHistoryPage = lazy(() => import("./pages/student/TestHistoryPage"));

const AllRoutes = () => {
  return (
    <>
      <Suspense fallback={<LoadingPage />}>
        <Routes>
          {/* Private Routes */}

          <Route path={"/dashboard"} element={<PrivateRoutes />}>
            <Route index element={<DashboardPage />} />
            <Route path={"/dashboard/tests"} element={<YourTestPage />} />
          </Route>

          <Route path={"/history"} element={<PrivateRoutes />}>
            <Route index element={<TestHistoryPage />} />
          </Route>

          <Route path={"/vocabulary"} element={<PrivateRoutes />}>
            <Route index element={<VocabularyPage />} />
            <Route path={"/vocabulary/:topicName"} element={<VocabularyPage />} />
          </Route>

          <Route path={"/account"} element={<PrivateRoutes />}>
            <Route index element={<AccountPage />} />
          </Route>

          {/* Admin Routes */}
          <Route path="/admin" element={<PrivateRoutes />}>
            <Route element={<AdminLayout />}>
              <Route path="dashboard" element={<AdminDashboard />} />
              <Route path="users" element={<UsersManagement />} />
              <Route path="exams" element={<ExamsManagement />} />
              <Route path="examparts" element={<ExamPartsManagement />} />
              <Route path="questions" element={<QuestionsManagement />} />
              <Route path="userexams" element={<UserExamsManagement />} />
              <Route path="vocabulary" element={<VocabularyManagement />} />
              <Route path="rag-examples" element={<RAGExamplesManagement />} />
            </Route>
          </Route>

          <Route path={"/"} element={<HomePage />} />
          <Route path={"/login"} element={<LoginPage />} />
          <Route path={"register"} element={<RegisterPage />} />          <Route path={"/verify-email"} element={<VerifyEmailPage />} />          <Route path={"/reset"} element={<ResetPasswordPage />} />
          <Route path={"/reset/confirm/"} element={<ConfirmResetPassword />} />

          {/* IELTS Tests */}
          <Route path={"/ieltstest"}>
            {/* Full Test Result Routes - without /attempt/ prefix */}
            <Route path="fulltest" element={<PrivateRoutes />}>
              <Route path="result/:userExamId" element={<FullTestResultPage />} />
              <Route path=":examId/result" element={<FullTestResultPage />} />
            </Route>
            
            {/* Skill Result Routes - without /attempt/ prefix */}
            <Route path="listening">
              <Route index element={<ModuleHomePage />} />
              <Route path="result/:userExamPartId" element={<ListeningResultPage />} />
            </Route>
            <Route path="reading">
              <Route index element={<ModuleHomePage />} />
              <Route path="result/:userExamPartId" element={<ReadingResultPage />} />
            </Route>
            <Route path="writing">
              <Route index element={<ModuleHomePage />} />
              <Route path="result/:userExamPartId" element={<WritingResultPage />} />
            </Route>
            <Route path="speaking">
              <Route index element={<ModuleHomePage />} />
              <Route path="result/:userExamPartId" element={<SpeakingResultPage />} />
            </Route>
            
            {/* Fallback for other modules */}
            <Route path={":module_slug"} element={<ModuleHomePage />} />
            
            <Route path="attempt">
              <Route
                path="listening/:module_slug/:attempt_slug/get_result"
                element={<ListeningResultPage />}
              />
              {/* Listening result by userExamPartId */}
              <Route
                path="listening/result/:userExamPartId"
                element={<ListeningResultPage />}
              />
              {/* Listening result by examId (legacy pattern) */}
              <Route
                path="listening/:examId/result"
                element={<ListeningResultPage />}
              />
              
              <Route
                path="reading/:module_slug/:attempt_slug/get_result"
                element={<ReadingResultPage />}
              />
              {/* Reading result by userExamPartId */}
              <Route
                path="reading/result/:userExamPartId"
                element={<ReadingResultPage />}
              />
              {/* Reading result by examId (legacy pattern) */}
              <Route
                path="reading/:examId/result"
                element={<ReadingResultPage />}
              />
              
              {/* Writing result by userExamPartId */}
              <Route
                path="writing/result/:userExamPartId"
                element={<WritingResultPage />}
              />
              
              {/* Speaking result by userExamPartId */}
              <Route
                path="speaking/result/:userExamPartId"
                element={<SpeakingResultPage />}
              />
            </Route>

            <Route path="attempt" element={<PrivateRoutes />}>
              {/* Full Test - New */}
              <Route
                path="fulltest/:examId"
                element={<AttemptFullTestPage />}
              />
              
              {/* Full Test Result Page - by userExamId */}
              <Route
                path="fulltest/result/:userExamId"
                element={<FullTestResultPage />}
              />
              
              {/* Full Test Result Page - by examId (legacy) */}
              <Route
                path="fulltest/:examId/result"
                element={<FullTestResultPage />}
              />
              
              {/* Full Test Info - Old */}
              <Route
                path="fulltest/info/:attempt_slug"
                element={<FullTestInfoPage />}
              />

              {/* Listening Attempt - Order matters! Single param routes must come BEFORE multi-param routes */}
              {/* New route for listening by exam ID (single param) */}
              <Route
                path="listening/:examId"
                element={<AttemptListeningModulePage />}
              />
              {/* Legacy route for listening (2 params) */}
              <Route
                path="listening/:module_slug/:attempt_slug"
                element={<AttemptListeningModulePage />}
              />

              {/* Reading Attempt - Order matters! Single param routes must come BEFORE multi-param routes */}
              {/* New route for reading by exam ID (single param) */}
              <Route
                path="reading/:examId"
                element={<AttemptReadingModulePage />}
              />
              {/* Legacy route for reading (2 params) */}
              <Route
                path="reading/:module_slug/:attempt_slug"
                element={<AttemptReadingModulePage />}
              />

              {/* Writing Attempt */}
              <Route
                path="writing/:examId"
                element={<AttemptWritingModulePage />}
              />
              
              {/* Writing Results */}
              <Route
                path="writing/:examId/result"
                element={<WritingResultPage />}
              />

              {/* Speaking Attempt - Using examId */}
              <Route
                path="speaking/:examId"
                element={<AttemptSpeakingExamPage />}
              />
            </Route>
          </Route>
        </Routes>
      </Suspense>
    </>
  );
};

export default AllRoutes;
