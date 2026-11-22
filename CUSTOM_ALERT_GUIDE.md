# Custom Alert System - Hướng dẫn sử dụng

## Tổng quan
Hệ thống custom alert đẹp mắt và dễ sử dụng cho toàn bộ ứng dụng, thay thế cho `window.alert()` và Bootstrap Alert.

## Cấu trúc
```
src/
├── context/
│   └── AlertContext.jsx          # Context quản lý alerts
├── components/
│   └── common/
│       └── CustomAlert.jsx       # Component hiển thị alerts
└── styles/
    └── CustomAlert.css           # Styles cho alerts
```

## Cách sử dụng

### 1. Import hook
```javascript
import { useAlert } from '../../context/AlertContext';
```

### 2. Sử dụng trong component
```javascript
const MyComponent = () => {
  const { showSuccess, showError, showWarning, showInfo } = useAlert();

  const handleSuccess = () => {
    showSuccess('Lưu thành công!');
  };

  const handleError = () => {
    showError('Có lỗi xảy ra, vui lòng thử lại!');
  };

  const handleWarning = () => {
    showWarning('Dữ liệu chưa được lưu!');
  };

  const handleInfo = () => {
    showInfo('Đây là thông báo quan trọng');
  };

  return (
    // Your JSX
  );
};
```

### 3. Các phương thức có sẵn

#### `showSuccess(message, duration)`
Hiển thị thông báo thành công (màu xanh lá)
```javascript
showSuccess('Tài khoản đã được tạo thành công!');
showSuccess('Đã lưu!', 3000); // Tự động đóng sau 3 giây
```

#### `showError(message, duration)`
Hiển thị thông báo lỗi (màu đỏ)
```javascript
showError('Email hoặc mật khẩu không đúng!');
showError('Kết nối thất bại!', 5000);
```

#### `showWarning(message, duration)`
Hiển thị cảnh báo (màu vàng)
```javascript
showWarning('Bạn chưa hoàn thành bài kiểm tra!');
```

#### `showInfo(message, duration)`
Hiển thị thông tin (màu xanh dương)
```javascript
showInfo('Phiên làm việc sắp hết hạn');
```

#### `showAlert(message, type, duration)`
Phương thức tổng quát
```javascript
showAlert('Custom message', 'success', 4000);
// type: 'success', 'error', 'warning', 'info'
```

### 4. Tùy chỉnh thời gian hiển thị
- **Mặc định**: 5000ms (5 giây)
- **Không tự động đóng**: `duration = 0` hoặc `duration = -1`
- **Tùy chỉnh**: Truyền số milliseconds

```javascript
showSuccess('Không tự đóng', 0);
showError('Đóng sau 3 giây', 3000);
showWarning('Đóng sau 10 giây', 10000);
```

## Ví dụ thực tế

### Ví dụ 1: Form đăng nhập
```javascript
import { useAlert } from '../../context/AlertContext';

const LoginPage = () => {
  const { showSuccess, showError } = useAlert();
  const navigate = useNavigate();

  const handleLogin = async (credentials) => {
    try {
      const response = await login(credentials);
      if (response.code === 200) {
        showSuccess('Đăng nhập thành công!');
        navigate('/dashboard');
      }
    } catch (error) {
      showError('Email hoặc mật khẩu không đúng!');
    }
  };

  return (
    // Form JSX
  );
};
```

### Ví dụ 2: Cập nhật thông tin
```javascript
import { useAlert } from '../../context/AlertContext';

const AccountSettings = () => {
  const { showSuccess, showError, showWarning } = useAlert();

  const handleSave = async (data) => {
    if (!data.email) {
      showWarning('Vui lòng nhập email!');
      return;
    }

    try {
      await updateProfile(data);
      showSuccess('Cập nhật thông tin thành công!');
    } catch (error) {
      showError('Không thể cập nhật. Vui lòng thử lại!');
    }
  };

  return (
    // Form JSX
  );
};
```

### Ví dụ 3: Xóa dữ liệu
```javascript
import { useAlert } from '../../context/AlertContext';

const DataManager = () => {
  const { showSuccess, showError, showInfo } = useAlert();

  const handleDelete = async (id) => {
    showInfo('Đang xử lý...', 2000);

    try {
      await deleteItem(id);
      showSuccess('Đã xóa thành công!');
    } catch (error) {
      showError('Không thể xóa. Vui lòng thử lại!');
    }
  };

  return (
    // Component JSX
  );
};
```

### Ví dụ 4: Vocabulary Page
```javascript
import { useAlert } from '../../context/AlertContext';

const VocabularyPage = () => {
  const { showSuccess, showError } = useAlert();

  const fetchVocabularies = async (topic) => {
    try {
      const response = await getVocabulariesByTopic(topic);
      if (response.code === 200) {
        setVocabularies(response.result);
      }
    } catch (err) {
      showError('Không thể tải từ vựng. Vui lòng thử lại!');
    }
  };

  const handleMarkAsMastered = () => {
    showSuccess('Đã đánh dấu từ này là đã thuộc!', 2000);
  };

  return (
    // Component JSX
  );
};
```

## Tính năng

✅ **4 loại alert**: Success, Error, Warning, Info
✅ **Animation mượt mà**: Slide in/out từ bên phải
✅ **Progress bar**: Hiển thị thời gian còn lại
✅ **Auto close**: Tự động đóng sau thời gian xác định
✅ **Manual close**: Người dùng có thể đóng bằng nút X
✅ **Stack alerts**: Hiển thị nhiều alert cùng lúc
✅ **Responsive**: Hoạt động tốt trên mobile
✅ **Beautiful design**: Gradient background, icons, shadows

## Thay thế Alert cũ

### ❌ Trước đây (không nên dùng)
```javascript
alert('Thành công!');
window.alert('Lỗi!');
<Alert variant="danger">Error message</Alert>
```

### ✅ Bây giờ (nên dùng)
```javascript
showSuccess('Thành công!');
showError('Lỗi!');
// Alert tự động hiển thị dưới dạng toast notification
```

## Ghi chú
- Alert sẽ xuất hiện ở góc **trên bên phải** màn hình
- Tối đa **4 alerts** hiển thị cùng lúc
- Alert thứ 5 trở đi sẽ bị ẩn để tránh lộn xộn
- Có thể click vào nút X để đóng alert bất cứ lúc nào
- Progress bar hiển thị thời gian còn lại trước khi alert tự đóng
