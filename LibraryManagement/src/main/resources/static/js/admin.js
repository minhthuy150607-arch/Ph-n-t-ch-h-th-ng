/**
 * ADMIN.JS - JavaScript cho khu vực quản trị
 * Xử lý: sidebar toggle, header user menu, datetime, search, notifications
 */

document.addEventListener('DOMContentLoaded', function () {

    // ===== SIDEBAR TOGGLE =====
    const sidebarToggle = document.getElementById('sidebarToggle');
    const adminSidebar = document.getElementById('adminSidebar');
    const sidebarOverlay = document.getElementById('sidebarOverlay');

    if (sidebarToggle && adminSidebar) {
        sidebarToggle.addEventListener('click', function () {
            adminSidebar.classList.toggle('active');
            if (sidebarOverlay) {
                sidebarOverlay.classList.toggle('active');
            }
        });
    }

    // Đóng sidebar khi click overlay
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', function () {
            adminSidebar.classList.remove('active');
            sidebarOverlay.classList.remove('active');
        });
    }

    // ===== HEADER USER MENU =====
    const headerUserBtn = document.getElementById('headerUserBtn');
    const headerUserMenu = headerUserBtn ? headerUserBtn.closest('.header-user-menu') : null;

    if (headerUserBtn && headerUserMenu) {
        headerUserBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            headerUserMenu.classList.toggle('active');
        });
    }

    // Đóng dropdown khi click ra ngoài
    document.addEventListener('click', function (e) {
        if (headerUserMenu && !headerUserMenu.contains(e.target)) {
            headerUserMenu.classList.remove('active');
        }
    });

    // ===== HIỂN THỊ NGÀY GIỜ HIỆN TẠI =====
    const headerDatetime = document.getElementById('headerDatetime');
    const currentDateEl = document.getElementById('currentDate');

    function updateDateTime() {
        const now = new Date();
        const days = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
        const dayName = days[now.getDay()];

        const dateStr = dayName + ', ' +
            String(now.getDate()).padStart(2, '0') + '/' +
            String(now.getMonth() + 1).padStart(2, '0') + '/' +
            now.getFullYear();

        const timeStr = String(now.getHours()).padStart(2, '0') + ':' +
            String(now.getMinutes()).padStart(2, '0');

        const datetimeText = document.getElementById('datetimeText');
        if (datetimeText) {
            datetimeText.textContent = dateStr + ' • ' + timeStr;
        } else if (headerDatetime) {
            headerDatetime.textContent = dateStr + ' ' + timeStr;
        }
        if (currentDateEl) {
            currentDateEl.textContent = dateStr + ' ' + timeStr;
        }
    }

    updateDateTime();
    setInterval(updateDateTime, 30000); // Cập nhật mỗi 30 giây

    // ===== GLOBAL SEARCH =====
    const globalSearch = document.getElementById('globalSearch');
    if (globalSearch) {
        let searchTimeout;
        globalSearch.addEventListener('input', function () {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(function () {
                const query = globalSearch.value.trim();
                if (query.length >= 2) {
                    // Chức năng tìm kiếm sẽ được triển khai ở Phase sau
                    console.log('Tìm kiếm:', query);
                }
            }, 300);
        });

        globalSearch.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                const query = globalSearch.value.trim();
                if (query) {
                    // Redirect to search results (sẽ triển khai sau)
                    console.log('Enter search:', query);
                }
            }
        });
    }

    // ===== TOAST CONTAINER (Reuse from main.js nếu chưa có) =====
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);
    }

    // Hàm showToast (nếu chưa được định nghĩa từ main.js)
    if (typeof window.showToast !== 'function') {
        window.showToast = function (message, type, duration) {
            type = type || 'success';
            duration = duration || 3000;
            var toast = document.createElement('div');
            toast.className = 'toast toast-' + type;

            var icons = {
                success: 'fa-check-circle',
                error: 'fa-exclamation-circle',
                warning: 'fa-exclamation-triangle'
            };

            toast.innerHTML =
                '<i class="fas ' + (icons[type] || icons.success) + '"></i>' +
                '<span>' + message + '</span>';

            toastContainer.appendChild(toast);

            setTimeout(function () {
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(100%)';
                setTimeout(function () {
                    toast.remove();
                }, 300);
            }, duration);
        };
    }

    // ===== CONFIRM DELETE =====
    if (typeof window.confirmDelete !== 'function') {
        window.confirmDelete = function (message) {
            return confirm(message || 'Bạn có chắc chắn muốn xóa?');
        };
    }

    // ===== TABLE ROW CLICK (Optional: click hàng trong bảng để xem chi tiết) =====
    document.querySelectorAll('.data-table tbody tr[data-href]').forEach(function (row) {
        row.style.cursor = 'pointer';
        row.addEventListener('click', function (e) {
            // Không chuyển trang nếu click vào nút hoặc link trong hàng
            if (e.target.closest('a, button, .actions')) return;
            window.location.href = this.dataset.href;
        });
    });

    // ===== AUTO DISMISS ALERTS =====
    document.querySelectorAll('.alert[data-auto-dismiss]').forEach(function (alert) {
        var duration = parseInt(alert.dataset.autoDismiss) || 5000;
        setTimeout(function () {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            setTimeout(function () {
                alert.remove();
            }, 300);
        }, duration);
    });
});
