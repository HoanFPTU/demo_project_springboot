/**
 * Sidebar toggle functionality with localStorage state persistence
 */
document.addEventListener('DOMContentLoaded', function () {
    // Đảm bảo đã tải DOM trước khi truy cập các phần tử
    initializeSidebar();
});

/**
 * Khởi tạo chức năng sidebar
 */
function initializeSidebar() {
    const sidebar = document.getElementById('sidebar');
    const main = document.getElementById('main');
    const sidebarCollapseBtn = document.getElementById('sidebarCollapseBtn');
    const sidebarToggle = document.getElementById('sidebarToggle');

    // Nếu không tìm thấy các phần tử cần thiết, thử lại sau 100ms
    if (!sidebar || !main) {
        console.log('Sidebar or Main element not found, retrying...');
        setTimeout(initializeSidebar, 100);
        return;
    }

    // Sidebar states
    const SIDEBAR_STATE = {
        EXPANDED: 'expanded',
        COLLAPSED: 'collapsed',
        HIDDEN: 'hidden'
    };

    // LocalStorage key
    const STORAGE_KEY = 'lms_sidebar_state';

    /**
     * Get saved sidebar state from localStorage
     * @returns {string} - Sidebar state
     */
    function getSavedState() {
        return localStorage.getItem(STORAGE_KEY) || SIDEBAR_STATE.EXPANDED;
    }

    /**
     * Save sidebar state to localStorage
     * @param {string} state - State to save
     */
    function saveState(state) {
        localStorage.setItem(STORAGE_KEY, state);
    }

    /**
     * Chuyển đổi hiển thị module để phù hợp với trạng thái sidebar
     * @param {boolean} isCollapsed - Có phải trạng thái thu gọn hay không
     */
    function toggleModuleDisplay(isCollapsed) {
        // Lấy tất cả module trong module groups
        const moduleGroups = document.querySelectorAll('.module-group-item');

        if (isCollapsed) {
            // Khi sidebar bị collapse, ẩn tất cả module group
            moduleGroups.forEach(group => {
                group.style.display = 'none';
            });

            // Lấy tất cả các module từ trong các group và tạo các mục riêng lẻ
            extractAndShowModulesFromGroups();
        } else {
            // Khi sidebar mở rộng, hiển thị lại tất cả module group
            moduleGroups.forEach(group => {
                group.style.display = '';
            });

            // Ẩn các module đã được trích xuất
            hideExtractedModules();
        }
    }

    /**
     * Trích xuất và hiển thị tất cả các module từ trong các group
     */
    function extractAndShowModulesFromGroups() {
        // Lấy tất cả các module con từ trong các group
        const moduleItems = document.querySelectorAll('.module-group-item .module-item');

        // Container để chứa các module flat
        const flatModulesContainer = document.getElementById('flat-modules-container');

        // Xóa nội dung cũ nếu có
        if (flatModulesContainer) {
            flatModulesContainer.innerHTML = '';

            // Hiển thị container trước khi thêm các module mới
            flatModulesContainer.style.display = 'block';

            // Tạo các module flat mới
            moduleItems.forEach(module => {
                const url = module.getAttribute('data-url');
                const name = module.getAttribute('data-name');
                const icon = module.getAttribute('data-icon');
                const isActive = module.classList.contains('active');

                if (url && name && icon) {
                    // Tạo mục mới
                    const li = document.createElement('li');
                    li.className = 'module-flat-item';

                    // Tạo link
                    const a = document.createElement('a');
                    a.className = 'nav-link';
                    if (isActive) {
                        a.classList.add('active');
                    }
                    a.href = '/' + url; // Đảm bảo có dấu / ở đầu URL

                    // Thêm icon
                    const iconElement = document.createElement('i');
                    iconElement.className = 'fa ' + icon;
                    iconElement.setAttribute('aria-hidden', 'true');
                    a.appendChild(iconElement);

                    // Thêm tên (đảm bảo hiển thị ngay cả khi sidebar collapsed)
                    const spanElement = document.createElement('span');
                    spanElement.textContent = name;

                    // Đảm bảo tên ngắn gọn và phù hợp với chiều rộng
                    if (name.length > 12) {
                        // Cắt tên nếu quá dài và thêm dấu ...
                        spanElement.setAttribute('title', name); // Thêm tooltip hiển thị tên đầy đủ

                        // Nếu tên có nhiều từ, hiển thị mỗi từ trên một dòng
                        if (name.includes(' ')) {
                            // Thay thế khoảng trắng bằng dấu xuống dòng
                            const words = name.split(' ');
                            if (words.length > 2) {
                                // Nếu có nhiều hơn 2 từ, hiển thị 2 từ đầu tiên và dấu ...
                                spanElement.innerHTML = words.slice(0, 2).join('<br>') + '...';
                            } else {
                                // Nếu chỉ có 2 từ, hiển thị trên 2 dòng
                                spanElement.innerHTML = words.join('<br>');
                            }
                        } else {
                            // Nếu là một từ dài, cắt bớt
                            spanElement.textContent = name.substring(0, 10) + '...';
                        }
                    }

                    a.appendChild(spanElement);

                    // Thêm vào sidebar
                    li.appendChild(a);
                    flatModulesContainer.appendChild(li);
                }
            });

            // Nếu có module active, scroll đến vị trí của module đó
            const activeModule = flatModulesContainer.querySelector('.active');
            if (activeModule) {
                setTimeout(() => {
                    activeModule.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }, 300);
            }
        }
    }

    /**
     * Ẩn các module đã được trích xuất
     */
    function hideExtractedModules() {
        const flatModulesContainer = document.getElementById('flat-modules-container');
        if (flatModulesContainer) {
            flatModulesContainer.style.display = 'none';
            flatModulesContainer.innerHTML = ''; // Xóa nội dung để giải phóng bộ nhớ
        }
    }

    /**
     * Apply sidebar state based on the given state
     * @param {string} state - State to apply
     */
    function applySidebarState(state) {
        // Remove all state classes first
        sidebar.classList.remove('collapsed', 'hidden');
        main.classList.remove('expanded', 'full-width');

        switch (state) {
            case SIDEBAR_STATE.COLLAPSED:
                sidebar.classList.add('collapsed');
                main.classList.add('expanded');

                // Đảm bảo dropdown menu hoạt động trong chế độ collapsed
                setupCollapsedMenu();

                // Chuyển đổi hiển thị module khi sidebar collapse
                toggleModuleDisplay(true);

                // Đảm bảo sidebar có thể cuộn
                setupSidebarScrolling();

                // Điều chỉnh chiều rộng sidebar
                sidebar.style.width = '80px';
                break;
            case SIDEBAR_STATE.HIDDEN:
                sidebar.classList.add('hidden');
                main.classList.add('full-width');
                break;
            default:
                // Expanded state - xử lý các sự kiện dropdown menu bình thường
                resetCollapsedMenu();

                // Chuyển đổi hiển thị module khi sidebar mở rộng
                toggleModuleDisplay(false);

                // Đảm bảo sidebar có thể cuộn
                setupSidebarScrolling();

                // Khôi phục chiều rộng sidebar
                sidebar.style.width = '';
        }
    }

    /**
     * Thiết lập menu dropdown cho chế độ sidebar thu gọn
     */
    function setupCollapsedMenu() {
        // Xử lý hiển thị menu con khi hover
        const navItems = sidebar.querySelectorAll('.nav-item');

        // Thêm sự kiện hover cho mỗi nav item
        navItems.forEach(item => {
            // Xóa các event listener cũ nếu có
            item.removeEventListener('mouseenter', showDropdown);
            item.removeEventListener('mouseleave', hideDropdown);

            // Thêm event listener mới
            item.addEventListener('mouseenter', showDropdown);
            item.addEventListener('mouseleave', hideDropdown);
        });
    }

    /**
     * Reset menu dropdown về chế độ bình thường
     */
    function resetCollapsedMenu() {
        // Xóa các sự kiện hover
        const navItems = sidebar.querySelectorAll('.nav-item');

        navItems.forEach(item => {
            item.removeEventListener('mouseenter', showDropdown);
            item.removeEventListener('mouseleave', hideDropdown);
        });
    }

    /**
     * Hiển thị dropdown khi hover
     */
    function showDropdown() {
        if (!sidebar.classList.contains('collapsed')) return;

        const collapse = this.querySelector('.collapse');
        if (collapse) {
            // Tính toán vị trí hiển thị
            const rect = this.getBoundingClientRect();
            collapse.style.top = rect.top + 'px';

            // Hiển thị dropdown
            collapse.classList.add('show');
        }
    }

    /**
     * Ẩn dropdown khi không hover
     */
    function hideDropdown() {
        if (!sidebar.classList.contains('collapsed')) return;

        const collapse = this.querySelector('.collapse');
        if (collapse) {
            collapse.classList.remove('show');
        }
    }

    /**
     * Kiểm tra URL hiện tại và áp dụng trạng thái sidebar tùy theo trang
     */
    function checkSpecificPageRules() {
        const currentPath = window.location.pathname;

        // Tự động thu gọn sidebar khi ở trang My Courses
        if (currentPath.includes('/student-page/courses')) {
            applySidebarState(SIDEBAR_STATE.COLLAPSED);
            saveState(SIDEBAR_STATE.COLLAPSED);
            return true;
        }

        return false;
    }

    // Kiểm tra quy tắc đặc biệt cho một số trang nhất định
    const specificRuleApplied = checkSpecificPageRules();

    // Chỉ áp dụng trạng thái từ localStorage nếu không có quy tắc đặc biệt nào được áp dụng
    if (!specificRuleApplied) {
        // Initialize sidebar state from localStorage
        applySidebarState(getSavedState());
    }

    // Handle sidebar collapse button click (toggle between expanded and collapsed)
    if (sidebarCollapseBtn) {
        console.log('Sidebar collapse button found, attaching event listener');

        // Đảm bảo loại bỏ event listener cũ nếu có
        const newCollapseBtn = sidebarCollapseBtn.cloneNode(true);
        sidebarCollapseBtn.parentNode.replaceChild(newCollapseBtn, sidebarCollapseBtn);

        // Thêm event listener mới
        newCollapseBtn.addEventListener('click', function (e) {
            console.log('Sidebar collapse button clicked');
            e.preventDefault();
            e.stopPropagation();

            const currentState = getSavedState();

            // Toggle between expanded and collapsed
            if (currentState === SIDEBAR_STATE.EXPANDED) {
                applySidebarState(SIDEBAR_STATE.COLLAPSED);
                saveState(SIDEBAR_STATE.COLLAPSED);
            } else {
                applySidebarState(SIDEBAR_STATE.EXPANDED);
                saveState(SIDEBAR_STATE.EXPANDED);
            }
        });
    } else {
        console.error('Sidebar collapse button not found! ID: sidebarCollapseBtn');
    }

    // Handle mobile sidebar toggle (show/hide)
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function () {
            if (sidebar.classList.contains('hidden')) {
                applySidebarState(SIDEBAR_STATE.EXPANDED);
                saveState(SIDEBAR_STATE.EXPANDED);
            } else {
                applySidebarState(SIDEBAR_STATE.HIDDEN);
                saveState(SIDEBAR_STATE.HIDDEN);
            }
        });
    }

    // Handle window resize events
    window.addEventListener('resize', function () {
        // For small screens, we want to automatically hide the sidebar
        // but keep the state saved so it can be restored when the screen expands
        if (window.innerWidth < 992) {
            if (getSavedState() !== SIDEBAR_STATE.HIDDEN) {
                // Only apply the hidden state, don't save it
                sidebar.classList.add('hidden');
                main.classList.add('full-width');
            }
        } else {
            // Restore the saved state on larger screens
            applySidebarState(getSavedState());
        }
    });

    // Xử lý riêng các module group trong sidebar để đảm bảo có thể đóng/mở
    const moduleGroupLinks = document.querySelectorAll('.sidebar .nav-link[data-bs-toggle="collapse"]');

    // Xóa tất cả các sự kiện collapse mặc định của Bootstrap để tự xử lý hoàn toàn
    moduleGroupLinks.forEach(link => {
        // Loại bỏ sự kiện data-bs-toggle mặc định
        link.setAttribute('data-bs-toggle-disabled', 'collapse');
        link.removeAttribute('data-bs-toggle');

        // Thêm sự kiện click mới
        link.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();

            const targetId = this.getAttribute('href');
            if (!targetId) return;

            const targetCollapse = document.querySelector(targetId);
            if (!targetCollapse) return;

            // Toggle phần tử hiện tại (đóng nếu đang mở, mở nếu đang đóng)
            if (targetCollapse.classList.contains('show')) {
                // Đang mở thì đóng lại
                this.setAttribute('aria-expanded', 'false');
                // Cập nhật icon
                const icon = this.querySelector('.fa-chevron-down');
                if (icon) {
                    icon.classList.remove('fa-rotate-180');
                }

                // Thêm hiệu ứng đóng mượt mà
                targetCollapse.style.maxHeight = '0px';
                // Đảm bảo class show bị xóa sau khi animation hoàn tất
                setTimeout(() => {
                    targetCollapse.classList.remove('show');
                }, 400); // Phải phù hợp với thời gian transition trong CSS
            } else {
                // Đóng tất cả các collapse khác trước
                document.querySelectorAll('.sidebar .collapse.show').forEach(openCollapse => {
                    if (openCollapse.id !== targetCollapse.id.substring(1)) {
                        // Đóng mượt mà
                        openCollapse.style.maxHeight = '0px';

                        // Cập nhật aria-expanded và icon cho link tương ứng
                        const otherLink = document.querySelector(`[href="#${openCollapse.id}"]`);
                        if (otherLink) {
                            otherLink.setAttribute('aria-expanded', 'false');
                            const otherIcon = otherLink.querySelector('.fa-chevron-down');
                            if (otherIcon) {
                                otherIcon.classList.remove('fa-rotate-180');
                            }
                        }

                        // Xóa class show sau khi animation kết thúc
                        setTimeout(() => {
                            openCollapse.classList.remove('show');
                        }, 400);
                    }
                });

                // Mở collapse hiện tại
                targetCollapse.classList.add('show');
                // Đặt max-height để tạo hiệu ứng slide-down
                const scrollHeight = targetCollapse.scrollHeight;
                targetCollapse.style.maxHeight = scrollHeight + 'px';

                this.setAttribute('aria-expanded', 'true');
                // Cập nhật icon
                const icon = this.querySelector('.fa-chevron-down');
                if (icon) {
                    icon.classList.add('fa-rotate-180');
                }
            }
        });
    });

    // Mở sẵn mục đang active (nếu có)
    const activeModuleLink = document.querySelector('.sidebar .nav-link.active');
    if (activeModuleLink) {
        // Tìm module group cha của module đang active
        const parentCollapse = activeModuleLink.closest('.collapse');
        if (parentCollapse) {
            // Mở module group chứa module đang active
            parentCollapse.classList.add('show');
            // Đặt max-height để hiển thị đúng
            parentCollapse.style.maxHeight = parentCollapse.scrollHeight + 'px';

            // Cập nhật aria-expanded và icon cho link tương ứng
            const parentCollapseToggle = document.querySelector(`[href="#${parentCollapse.id}"]`);
            if (parentCollapseToggle) {
                parentCollapseToggle.setAttribute('aria-expanded', 'true');
                const icon = parentCollapseToggle.querySelector('.fa-chevron-down');
                if (icon) {
                    icon.classList.add('fa-rotate-180');
                }
            }
        }
    }

    /**
     * Đảm bảo sidebar có thể cuộn khi có quá nhiều module
     */
    function setupSidebarScrolling() {
        // Lấy sidebar và container của các module
        const sidebar = document.getElementById('sidebar');
        const sidebarMenu = document.getElementById('sidebarAccordion');
        const flatModulesContainer = document.getElementById('flat-modules-container');

        // Đảm bảo sidebar có thể cuộn
        if (sidebar) {
            sidebar.style.overflowY = 'auto';
            sidebar.style.maxHeight = '100vh';

            // Xử lý khi có module active, cuộn đến vị trí của module đó
            const activeModule = sidebar.querySelector('.active');
            if (activeModule) {
                setTimeout(() => {
                    activeModule.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }, 300);
            }

            // Chỉ cho phép sidebar cuộn, các container bên trong không cần cuộn riêng
            if (sidebarMenu) {
                sidebarMenu.style.overflowY = 'visible';
                sidebarMenu.style.maxHeight = 'none';
            }

            if (flatModulesContainer) {
                flatModulesContainer.style.overflowY = 'visible';
                flatModulesContainer.style.maxHeight = 'none';
            }
        }
    }
} 