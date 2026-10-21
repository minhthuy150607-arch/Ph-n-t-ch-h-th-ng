/**
 * MAIN.JS - JavaScript cho phía bạn đọc (client)
 * Xử lý: menu mobile, user dropdown, password toggle, toast notifications
 */

document.addEventListener('DOMContentLoaded', function () {

    // ===== MOBILE MENU TOGGLE =====
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const mainNav = document.getElementById('mainNav');

    function closeMobileMenu() {
        if (mainNav && mainNav.classList.contains('active')) {
            mainNav.classList.remove('active');
            document.body.style.overflow = '';
            if (mobileMenuToggle) {
                const icon = mobileMenuToggle.querySelector('i');
                if (icon) {
                    icon.classList.remove('fa-times');
                    icon.classList.add('fa-bars');
                }
            }
        }
    }

    if (mobileMenuToggle && mainNav) {
        mobileMenuToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            const isActive = mainNav.classList.toggle('active');
            const icon = this.querySelector('i');
            if (isActive) {
                if (icon) {
                    icon.classList.remove('fa-bars');
                    icon.classList.add('fa-times');
                }
                document.body.style.overflow = 'hidden';
            } else {
                if (icon) {
                    icon.classList.remove('fa-times');
                    icon.classList.add('fa-bars');
                }
                document.body.style.overflow = '';
            }
        });
    }

    // ===== USER DROPDOWN MENU =====
    const userMenuBtn = document.getElementById('userMenuBtn');
    const userMenu = userMenuBtn ? userMenuBtn.closest('.user-menu') : null;

    if (userMenuBtn && userMenu) {
        userMenuBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            userMenu.classList.toggle('active');
        });
    }

    // Đóng dropdown khi click ra ngoài
    document.addEventListener('click', function (e) {
        // Đóng user dropdown
        if (userMenu && !userMenu.contains(e.target)) {
            userMenu.classList.remove('active');
        }
        // Đóng mobile menu khi click link hoặc click bên ngoài
        if (mainNav && mainNav.classList.contains('active')) {
            if (e.target.closest('.nav-link') || e.target.closest('.btn-login') || (!mainNav.contains(e.target) && !mobileMenuToggle.contains(e.target))) {
                closeMobileMenu();
            }
        }
    });

    // ===== PASSWORD TOGGLE (Trang đăng nhập) =====
    const passwordToggle = document.getElementById('passwordToggle');
    const passwordInput = document.getElementById('password');

    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener('click', function () {
            const icon = this.querySelector('i');
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                passwordInput.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    }

    // ===== TOAST NOTIFICATIONS =====
    // Tạo container cho toast nếu chưa có
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);
    }

    /**
     * Hiển thị toast notification.
     * @param {string} message - Nội dung thông báo
     * @param {string} type - Loại: 'success', 'error', 'warning'
     * @param {number} duration - Thời gian hiển thị (ms), mặc định 3000
     */
    window.showToast = function (message, type = 'success', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        const icons = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            warning: 'fa-exclamation-triangle'
        };

        toast.innerHTML = `
            <i class="fas ${icons[type] || icons.success}"></i>
            <span>${message}</span>
        `;

        toastContainer.appendChild(toast);

        // Tự động ẩn sau duration
        setTimeout(function () {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(function () {
                toast.remove();
            }, 300);
        }, duration);
    };

    // ===== CONFIRM DELETE =====
    /**
     * Hiển thị hộp thoại xác nhận xóa.
     * @param {string} message - Nội dung xác nhận
     * @returns {boolean}
     */
    window.confirmDelete = function (message) {
        return confirm(message || 'Bạn có chắc chắn muốn xóa?');
    };

    // ===== FORMAT NUMBER =====
    /**
     * Format số với dấu phân cách hàng nghìn.
     * @param {number} num
     * @returns {string}
     */
    window.formatNumber = function (num) {
        return new Intl.NumberFormat('vi-VN').format(num);
    };
});
