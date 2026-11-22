import { useEffect, useContext, useState } from "react";
import AuthContext, { useAuth } from "../../context/AuthContext";
import { Link, useNavigate } from "react-router-dom";
import { Col, Row, Card, Container, Button } from "react-bootstrap";
import BaseForm from "../../components/layout/BaseForm";
import * as Yup from "yup";
import YupPassword from "yup-password";

YupPassword(Yup);

const RegisterPage = () => {
  const navigate = useNavigate();
  let { registerUser, registrationError, user } = useContext(AuthContext);

  useEffect(() => {
    if (user) {
      navigate("/");
    }
  }, []);

  const form_fields = [
    {
      type: "text",
      label: "First Name",
      id: "first_name",
      invalid_feedback: "e.g. Rahul",
      placeholder: "Enter your First Name",
    },
    {
      type: "text",
      label: "Last Name",
      id: "last_name",
      invalid_feedback: "e.g. Sharma",
      placeholder: "Enter your Last Name",
    },
    {
      type: "email",
      label: "Email",
      id: "email",
      invalid_feedback: "e.g. yourname@gmail.com",
      placeholder: "Enter your Email Address",
    },
    {
      type: "tel",
      label: "Phone Number",
      id: "phoneNumber",
      invalid_feedback: "e.g. 0123456789",
      placeholder: "Enter your Phone Number",
    },
    {
      type: "select",
      label: "Gender",
      id: "gender",
      options: [
        { value: "", label: "Select your Gender" },
        { value: "MALE", label: "Male" },
        { value: "FEMALE", label: "Female" },
        { value: "OTHER", label: "Other" }
      ],
    },
    {
      type: "date",
      label: "Date of Birth",
      id: "dob",
      invalid_feedback: "You must be at least 18 years old",
      placeholder: "yyyy-mm-dd",
    },
    {
      type: "password",
      label: "Password",
      id: "password1",
      invalid_feedback: "Password should be strong",
      placeholder: "*************",
    },
    {
      type: "password",
      label: "Confirm Password",
      id: "password2",
      invalid_feedback: "Confirm password should  be same as Password",
      placeholder: "*************",
    },
  ];

  const SignupSchema = Yup.object().shape({
    first_name: Yup.string()
      .min(2, "Too Short!")
      .max(50, "Too Long!")
      .required("Required"),
    last_name: Yup.string()
      .min(2, "Too Short!")
      .max(50, "Too Long!")
      .required("Required"),
    email: Yup.string().email("Invalid email").required("Required"),
    phoneNumber: Yup.string()
      .matches(/^[0-9]{10}$/, "Phone number must be 10 digits")
      .required("Required"),
    gender: Yup.string()
      .oneOf(["MALE", "FEMALE", "OTHER"], "Invalid gender")
      .required("Required"),
    dob: Yup.date()
      .max(new Date(Date.now() - 567648000000), "You must be at least 18 years old")
      .required("Required"),
    password1: Yup.string()
      .required("Password is required")
      .min(
        6,
        "password must contain 6 or more characters"
      ),
    password2: Yup.string().oneOf(
      [Yup.ref("password1"), null],
      "Passwords must match"
    ),
  });

  return (
    <>
      <Container className="mb-5">
        <Row className="align-items-center justify-content-center ">
          <Col lg={5} md={8} className="">
            <Card className="mt-5">
              <Card.Body className="">
                <div className="mb-4">
                  <h1 className="mb-1 fw-bold">Sign up</h1>
                  <span>
                    Enter your personal information to create your account
                  </span>
                </div>

                <BaseForm
                  form_fields={form_fields}
                  submit_label={"Sign up"}
                  on_submit={registerUser}
                  serverErrors={registrationError}
                  validation_schema={SignupSchema}
                  successMessage="Registration Successful! Please check your email to verify your account."
                />
              </Card.Body>
              <Card.Footer>
                <Row>
                  <Col sm={12} md={6} className="my-2">
                    <Button
                      variant="outline-primary"
                      className="w-100"
                      as={Link}
                      to={"/login/"}
                    >
                      Sign In
                    </Button>
                  </Col>
                  <Col sm={12} md={6} className="my-2">
                    <Button
                      variant="outline-primary"
                      className="w-100"
                      as={Link}
                      to={"/reset/"}
                    >
                      Forgot Password
                    </Button>
                  </Col>
                </Row>
              </Card.Footer>
            </Card>
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default RegisterPage;
