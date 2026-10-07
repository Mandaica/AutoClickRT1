# Auto Click RT Advanced

Bản này có:
- Nhiều điểm click.
- Mỗi điểm có X/Y, số lần click, gap ms.
- Lịch tuyệt đối `yyyy-MM-dd HH:mm:ss.SSS` theo Asia/Bangkok (UTC+7).
- Scheduler chuyển từ wall-clock sang `elapsedRealtime()` sau khi tính deadline, giảm ảnh hưởng của thay đổi đồng hồ giữa lúc chờ.
- AccessibilityService dùng `dispatchGesture()` để tap.
- Overlay để lấy tọa độ.
- Start/Stop.

Lưu ý độ chính xác:
Android không đảm bảo click đúng tuyệt đối từng 1 ms. Scheduler có thể canh rất sát nhưng OS scheduling, tải máy, refresh rate và ứng dụng đích vẫn tạo jitter. `dispatchGesture()` cũng do Android quản lý.

Quyền:
1. Hiển thị trên ứng dụng khác.
2. Trợ năng.

Build:
Mở bằng Android Studio và Build > Build APK(s).
