import React from "react";
import { Link } from "react-router-dom";

const AdminDashboard = () => {
  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <h1>Admin Dashboard</h1>
      </div>

      <div className="dashboard-cards">
        <Link to="/admin/users" className="dashboard-card-link">
          <div className="dashboard-card">
            <div className="card-icon">👥</div>
            <div className="card-content">
              <h3>Users</h3>
              <p>Manage user accounts</p>
            </div>
          </div>
        </Link>

        <Link to="/admin/exams" className="dashboard-card-link">
          <div className="dashboard-card">
            <div className="card-icon">📝</div>
            <div className="card-content">
              <h3>Exams</h3>
              <p>Manage IELTS exams</p>
            </div>
          </div>
        </Link>

        <Link to="/admin/examparts" className="dashboard-card-link">
          <div className="dashboard-card">
            <div className="card-icon">📋</div>
            <div className="card-content">
              <h3>Exam Parts</h3>
              <p>Manage exam sections</p>
            </div>
          </div>
        </Link>

        <Link to="/admin/questions" className="dashboard-card-link">
          <div className="dashboard-card">
            <div className="card-icon">❓</div>
            <div className="card-content">
              <h3>Questions</h3>
              <p>Manage exam questions</p>
            </div>
          </div>
        </Link>
      </div>

      <style jsx>{`
        .dashboard-cards {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
          gap: 20px;
          margin-top: 30px;
        }

        .dashboard-card-link {
          text-decoration: none;
          color: inherit;
        }

        .dashboard-card {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          padding: 30px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          gap: 20px;
          box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
          transition: transform 0.3s ease, box-shadow 0.3s ease;
          cursor: pointer;
        }

        .dashboard-card:hover {
          transform: translateY(-5px);
          box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
        }

        .dashboard-card-link:nth-child(2) .dashboard-card {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        .dashboard-card-link:nth-child(3) .dashboard-card {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        .dashboard-card-link:nth-child(4) .dashboard-card {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }

        .card-icon {
          font-size: 3rem;
        }

        .card-content h3 {
          margin: 0 0 5px 0;
          font-size: 1.5rem;
        }

        .card-content p {
          margin: 0;
          opacity: 0.9;
          font-size: 0.95rem;
        }
      `}</style>
    </div>
  );
};

export default AdminDashboard;
