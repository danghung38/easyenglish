import React, { useEffect, useState } from "react";
import useAxios from "../../utils/useAxios";
import { Container, Row, Col, Card } from "react-bootstrap";
import * as Yup from "yup";
import BaseForm from "../layout/BaseForm";
import { DJANGO_BASE_URL } from "../../utils/config";

const AccountSettingForm = () => {
  const api = useAxios();
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
 
  const form_fields = [
    {
      type: "text",
      label: "Full Name",
      id: "fullName",
      invalid_feedback: "Please enter your full name",
      placeholder: "Enter your full name",
      value: userData?.name || "",
    },
    {
      type: "email",
      label: "Email",
      id: "email",
      disabled: true,
      placeholder: "Email",
      value: userData?.email || "",
    },
    {
      type: "text",
      label: "Phone Number",
      id: "phoneNumber",
      disabled: true,
      placeholder: "Phone Number",
      value: userData?.phoneNumber || "",
    },
    {
      type: "date",
      label: "Date of Birth",
      id: "dob",
      invalid_feedback: "Please enter your date of birth",
      placeholder: "yyyy/MM/dd",
      value: userData?.dob || "",
    },
    {
      type: "select",
      label: "Gender",
      id: "gender",
      invalid_feedback: "Please select your gender",
      placeholder: "Select your gender",
      value: userData?.gender || "",
      options: [
        { value: "MALE", label: "Male" },
        { value: "FEMALE", label: "Female" },
        { value: "OTHER", label: "Other" },
      ],
    },
    {
      type: "select",
      label: "Your Target Bands",
      id: "bandsTarget",
      invalid_feedback: "How many bands you're targeting?",
      placeholder: "Select your target bands",
      value: userData?.bandsTarget || "",
      options: [
        { value: 6, label: "6.0 Bands" },
        { value: 6.5, label: "6.5 Bands" },
        { value: 7, label: "7.0 Bands" },
        { value: 7.5, label: "7.5 Bands" },
        { value: 8, label: "8.0 Bands" },
        { value: 8.5, label: "8.5 Bands" },
        { value: 9, label: "9.0 Bands" },
      ],
    },
  ];

  const AccountSettingSchema = Yup.object().shape({
    fullName: Yup.string()
      .min(2, "Too Short!")
      .max(100, "Too Long!")
      .required("Full name is required"),
    dob: Yup.date()
      .max(new Date(), "Date of birth cannot be in the future")
      .nullable(),
    gender: Yup.string()
      .oneOf(["MALE", "FEMALE", "OTHER"], "Invalid gender")
      .nullable(),
    bandsTarget: Yup.number().required("You must select your target bands"),
  });

  const updateAccountSetting = async (values, handleSuccess) => {
    try {
      const formData = new FormData();
      
      // Only send fields that UserUpdateRequest accepts
      const updateData = {
        fullName: values.fullName,
        dob: values.dob ? values.dob.replace(/-/g, '/') : null, // Convert yyyy-MM-dd to yyyy/MM/dd
        gender: values.gender,
        bandsTarget: values.bandsTarget
      };
      
      // Create a Blob with Content-Type application/json for the user part
      const userBlob = new Blob([JSON.stringify(updateData)], {
        type: 'application/json'
      });
      formData.append("user", userBlob);
      
      const response = await api.put(
        DJANGO_BASE_URL + "/users",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );
      
      if (response.data.code === 200) {
        getUserDetails(); // Refresh data
        handleSuccess();
      }
    } catch (error) {
      console.error("Error updating account:", error.response || error.message);
    }
  };

  function getUserDetails() {
    setLoading(true);
    api
      .get(DJANGO_BASE_URL + "/users/myInfo")
      .then((response) => {
        if (response.status === 200 && response.data.code === 200) {
          console.log("User data received:", response.data.result);
          setUserData(response.data.result);
        }
      })
      .catch((error) => {
        console.error("Error fetching user details:", error);
      })
      .finally(() => {
        setLoading(false);
      });
  }

  useEffect(() => {
    getUserDetails();
  }, []);

  if (loading) {
    return (
      <Card>
        <Card.Header>
          <h3 className="mt-2 fw-bold">Personal Information & Preferences</h3>
        </Card.Header>
        <Card.Body>
          <div className="text-center py-5">
            <div className="spinner-border text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
            <p className="text-muted mt-3">Loading your information...</p>
          </div>
        </Card.Body>
      </Card>
    );
  }

  return (
    <Card>
      <Card.Header>
        <h3 className="mt-2 fw-bold">Personal Information & Preferences</h3>
        <p className="text-muted small mb-0">Update your profile details and IELTS target. Changes will be saved when you click Update.</p>
      </Card.Header>
      <Card.Body>
        <BaseForm
          form_fields={form_fields}
          submit_label="Update"
          successMessage="Your account settings have been updated successfully"
          validation_schema={AccountSettingSchema}
          on_submit={updateAccountSetting}
        />
      </Card.Body>
    </Card>
  );
};

export default AccountSettingForm;
