import { Link, Outlet, useLocation } from "react-router-dom";
import "../../assets/scss/_admin.scss";

const AdminLayout = () => {
  const location = useLocation();

  const menuItems = [
    { path: "/admin/dashboard", label: "Dashboard", icon: "📊" },
    { path: "/admin/users", label: "Users", icon: "👥" },
    { path: "/admin/exams", label: "Exams", icon: "📝" },
    { path: "/admin/examparts", label: "Exam Parts", icon: "📋" },
    { path: "/admin/questions", label: "Questions", icon: "❓" },
    { path: "/admin/userexams", label: "User Exams", icon: "📄" },
    { path: "/admin/vocabulary", label: "Vocabulary", icon: "📚" },
    { path: "/admin/rag-examples", label: "RAG Examples", icon: "🤖" },
  ];

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <div className="admin-sidebar-header">
          <h2>🎓 IELTS Admin</h2>
        </div>
        <nav className="admin-nav">
          <ul>
            {menuItems.map((item) => (
              <li
                key={item.path}
                className={location.pathname === item.path ? "active" : ""}
              >
                <Link to={item.path}>
                  <span className="icon">{item.icon}</span>
                  <span className="label">{item.label}</span>
                </Link>
              </li>
            ))}
          </ul>
        </nav>
        <div className="admin-sidebar-footer">
          <Link to="/" className="back-to-home">
            ← Back to Home
          </Link>
        </div>
      </aside>
      <main className="admin-content">
        <Outlet />
      </main>
    </div>
  );
};

export default AdminLayout;
