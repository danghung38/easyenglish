import React, { useEffect } from "react";
import { Container, Row, Col } from "react-bootstrap";
import AccountSettingForm from "../../components/auth/AccountSettingForm";
import ChangePasswordForm from "../../components/auth/ChangePasswordForm";

const AccountPage = () => {
  useEffect(() => {
    document.title = "Account Settings | KeenIELTS";
  }, []);

  return (
    <>
      <Container>
        <Row className="justify-content-center ">
          <Col xl={12} lg={12} md={12} className="mt-5 mb-4">
            <h2 className="fw-bold">Account Settings</h2>
            <p className="text-muted">Manage your personal information and IELTS preferences</p>
          </Col>
          <Col xl={6} lg={6} md={12} className="mb-3">
            <AccountSettingForm />
          </Col>
          <Col xl={6} lg={6} md={12} className="mb-3">
            <ChangePasswordForm />
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default AccountPage;
