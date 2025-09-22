// Global variables for role management
let currentPage = 0;
let totalPages = 0;
let sortBy = "name";
let sortDirection = "asc";
let currentView = "list";
let searchQuery = "";
let searchTimeout;

// ✅ Allowed sort fields (must match backend whitelist in RoleController & RoleService)
const allowedSortFields = ["id", "name"];

// Toggle between list and grid view
function toggleView() {
    currentView = currentView === "list" ? "grid" : "list";
    const viewToggleBtn = document.getElementById("viewToggleBtn");
    if (currentView === "grid") {
        viewToggleBtn.innerHTML = '<i class="fas fa-list"></i>';
        viewToggleBtn.title = "Switch to List View";
        document.getElementById("listView").classList.add("d-none");
        document.getElementById("gridView").classList.remove("d-none");
    } else {
        viewToggleBtn.innerHTML = '<i class="fas fa-th"></i>';
        viewToggleBtn.title = "Switch to Grid View";
        document.getElementById("gridView").classList.add("d-none");
        document.getElementById("listView").classList.remove("d-none");
    }
    fetchRoles(currentPage);
}

// Fetch roles with improved error handling
function fetchRoles(page = 0, query = searchQuery) {
	// ✅ Ensure safe sort before calling backend
    if (!allowedSortFields.includes(sortBy)) {
        console.warn(`Sort field "${sortBy}" is not allowed — resetting to "name".`);
        sortBy = "name";
    }

    if (page < 0) page = 0; // Prevent negative page
    let url = `/api/roles?page=${page}&size=10&sort=${sortBy},${sortDirection}`;
    if (query) url += `&search=${encodeURIComponent(query)}`;

    $("#roleTable, #roleGrid").html(`
        <div class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2 text-muted">Loading roles...</p>
        </div>
    `);

    return new Promise((resolve, reject) => {
        $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            timeout: 10000, // 10s timeout

            success: (data) => {
                console.log("Raw response from backend:", data);

                // unwrap if backend sends { data: {...} }
                if (data && data.data) {
                    data = data.data;
                }

                // sanity check in case backend sends an error JSON
                if (!data || !("totalPages" in data)) {
                    console.error("Unexpected response format", data);
                    $("#roleTable, #roleGrid").html(`
                        <div class="col-12 text-center py-4 text-danger">
                            <i class="fas fa-exclamation-circle fa-2x mb-3"></i>
                            <p>Unexpected response format from server</p>
                        </div>
                    `);
                    return;
                }

                totalPages = data.totalPages;
                currentPage = data.number;

                if (currentView === "list") {
                    renderListView(data.content);
                } else {
                    renderGridView(data.content);
                }

                updatePaginationControls();
                fetchTotalCount(query);
                resolve(data);
            },
            error: (error) => {
                let message = parseErrorResponse(error);
                if (error.status === 0) {
                    message = "Network error or server is unreachable.";
                } else if (error.status === 401) {
                    message = "Unauthorized access. Please log in.";
                } else if (error.status === 403) {
                    message = "Access denied. Insufficient permissions.";
                } else if (error.statusText === "timeout") {
                    message = "Request timed out. Please try again.";
                }
                $("#roleTable, #roleGrid").html(`
                    <div class="col-12 text-center py-4 text-danger">
                        <i class="fas fa-exclamation-circle fa-2x mb-3"></i>
                        <p>${message}</p>
                        <button class="btn btn-sm btn-outline-primary mt-2" id="retryBtn">Retry</button>
                    </div>
                `);
                $("#retryBtn").on("click", () => fetchRoles(currentPage, searchQuery));
                $("#totalRecords").text("0");
                showToast("Error", message, "danger", 4000);
                reject(error);
            }
        });
    });
}

// Fetch total count with error handling
function fetchTotalCount(query = searchQuery) {
    let countUrl = `/api/roles/count`;
    if (query) countUrl += `?search=${encodeURIComponent(query)}`;
    $.ajax({
        url: countUrl,
        type: "GET",
        dataType: "json",
        timeout: 5000,
        success: (data) => {
            const count = data.data;
            $("#totalRecords").text(count);
        },
        error: (error) => {
            $("#totalRecords").text("0");
            const message = parseErrorResponse(error);
            showToast("Error", `Failed to fetch role count: ${message}`, "danger", 4000);
        }
    });
}

// Update pagination controls
function updatePaginationControls() {
    let paginationHtml = "";
    if (currentPage > 0) {
        paginationHtml += `<button class="btn btn-outline-secondary" onclick="fetchRoles(${currentPage - 1})">< Prev</button>`;
    }
    paginationHtml += `<span class="mx-3">Page ${currentPage + 1} of ${totalPages}</span>`;
    if (currentPage < totalPages - 1) {
        paginationHtml += `<button class="btn btn-outline-secondary" onclick="fetchRoles(${currentPage + 1})">Next ></button>`;
    }
    $("#paginationControls").html(paginationHtml);
}

// Render roles in list view
function renderListView(roles) {
    let rows = "";
    if (roles && roles.length > 0) {
        roles.forEach((role, index) => {
            rows += `
                <tr>
                    <td class="text-center">${index + 1}</td>
                    <td>${role.name}</td>

                    <td class="text-start">
                        <button class="btn btn-outline-secondary me-1" onclick="editRole(${role.id}, '${role.name}')">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-outline-secondary" onclick="deleteRole(${role.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        });
    } else {
        rows = `
            <tr>
                <td colspan="4" class="text-center py-5">
                    <div class="d-flex flex-column align-items-center">
                        <i class="fas fa-search fa-3x text-muted mb-3"></i>
                        <h5>No roles found</h5>
                        <p class="text-muted">Try adjusting your search or filters</p>
                        <button class="btn btn-sm btn-outline-primary" id="inlineResetBtn">Reset</button>
                    </div>
                </td>
            </tr>
        `;
    }
    $("#roleTable").html(rows);
    $("#inlineResetBtn").on("click", () => {
        $("#reloadBtn").click();
    });
}

// Render roles in grid view
function renderGridView(roles) {
    let gridItems = '<div class="row">';
    if (roles && roles.length > 0) {
        roles.forEach((role) => {
            gridItems += `
                <div class="col-md-6 col-lg-4 mb-4">
                    <div class="card h-100">
                        <div class="card-header">
                            <h5 class="card-title">
                                <i class="fas fa-user-tag" aria-hidden="true"></i>
                                ${role.name}
                            </h5>
                        </div>
                        <div class="card-body">
                            <p class="card-text text-muted">Role ID: ${role.id}</p>
                        </div>
                        <div class="card-footer d-flex justify-content-between">

                            <div>
                                <button class="btn btn-outline-secondary me-1" onclick="editRole(${role.id}, '${role.name}')">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-outline-secondary" onclick="deleteRole(${role.id})">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
        gridItems += "</div>";
    } else {
        gridItems = `
            <div class="w-100 text-center py-5">
                <div class="d-flex flex-column align-items-center">
                    <i class="fas fa-search fa-3x text-muted mb-3"></i>
                    <h5>No roles found</h5>
                    <p class="text-muted">Try adjusting your search or filters</p>
                    <button class="btn btn-sm btn-outline-primary" id="inlineResetBtnGrid">Reset</button>
                </div>
            </div>
        `;
    }
    $("#roleGrid").html(gridItems);
    $("#inlineResetBtnGrid").on("click", () => {
        $("#reloadBtn").click();
    });
}

// Sort table by field
function sortTable(field) {
    // ✅ Frontend protection — revert to default if not in whitelist
    if (!allowedSortFields.includes(field)) {
        console.warn(`Sort field "${field}" is not allowed — defaulting to "name".`);
        field = "name";
    }
    if (sortBy === field) {
        sortDirection = sortDirection === "asc" ? "desc" : "asc";
    } else {
        sortBy = field;
        sortDirection = "asc";
    }
    fetchRoles();
}

// Update sort icons in table headers
function updateSortIcons() {
    $("th i.fa").removeClass("fa-sort-up fa-sort-down text-primary").addClass("fa-sort text-muted");
    const iconId = `#sort-${sortBy}`;
    $(iconId)
        .removeClass("fa-sort text-muted")
        .addClass(sortDirection === "asc" ? "fa-sort-up" : "fa-sort-down")
        .addClass("text-primary");
}

// Open modal to create new role
function createRole() {
    $("#roleForm")[0].reset();
    $("#roleId").val("");
    const roleFormModal = new bootstrap.Modal(document.getElementById("roleFormModal"));
    roleFormModal.show();
}

// Open modal to edit existing role
function editRole(id, name) {
    $("#roleId").val(id);
    $("#roleName").val(name);
    const roleFormModal = new bootstrap.Modal(document.getElementById("roleFormModal"));
    roleFormModal.show();
}

// Delete role with error handling
function deleteRole(id) {
    if (confirm("Are you sure you want to delete this role?")) {
        $.ajax({
            url: `/api/roles/${id}`,
            type: "DELETE",
            timeout: 5000,
            success: () => {
                showToast("Success", "Role deleted successfully", "success", 4000);
                fetchRoles();
            },
            error: (error) => {
                let message = parseErrorResponse(error);
                if (error.status === 404) {
                    message = "Role not found.";
                } else if (error.status === 403) {
                    message = "Access denied. Insufficient permissions.";
                }
                showToast("Error", message, "danger", 4000);
            }
        });
    }
}

// Reset role form
function resetForm() {
    $("#roleForm")[0].reset();
    $("#roleId").val("");
    $("#roleName").val("");
    const modal = bootstrap.Modal.getInstance(document.getElementById("roleFormModal"));
    if (modal) modal.hide();
}

// Display toast notification
function showToast(title, message, type, duration = 4000) {
    const toastContainer = document.getElementById("toastContainer");
    toastContainer.innerHTML = `
        <div class="toast align-items-center text-bg-${type} border-0" role="alert" aria-live="assertive" aria-atomic="true" data-bs-autohide="true" data-bs-delay="${duration}">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>${title}:</strong> ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    const toastElement = toastContainer.querySelector(".toast");
    const toast = new bootstrap.Toast(toastElement);
    toast.show();
}

// Helper function to parse error response
function parseErrorResponse(error) {
    let message = "An unexpected error occurred.";
    if (error.responseJSON && error.responseJSON.message) {
        message = error.responseJSON.message;
    } else if (error.responseText) {
        try {
            const json = JSON.parse(error.responseText);
            message = json.message || error.responseText;
        } catch (e) {
            message = error.responseText;
        }
    }
    return message;
}

// Export roles to Excel
function exportRoles() {
    $.ajax({
        url: "/api/roles/export",
        type: "GET",
        xhrFields: {
            responseType: "blob"
        },
        success: (data) => {
            data = data.data;
            const url = window.URL.createObjectURL(new Blob([data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", "roles.xlsx");
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            showToast("Success", "Roles exported successfully", "success", 4000);
        },
        error: (error) => {
            const message = parseErrorResponse(error);
            showToast("Error", `Failed to export roles: ${message}`, "danger", 4000);
        }
    });
}

// Initialize page
$(document).ready(() => {
    fetchRoles();

    const roleFormModal = new bootstrap.Modal($("#roleFormModal")[0], {
        backdrop: "static",
        keyboard: false,
        focus: true
    });

    const importModal = new bootstrap.Modal($("#importModal")[0], {
        backdrop: "static",
        keyboard: false,
        focus: true
    });

    $("#roleForm").on("submit", (e) => {
        e.preventDefault();
        const id = $("#roleId").val();
        const name = $("#roleName").val().trim();

        // Input validation
        if (!name) {
            showToast("Warning", "Role name is required!", "warning", 2000);
            $("#roleName").focus();
            return;
        }
        if (name.length > 50) {
            showToast("Warning", "Role name cannot exceed 50 characters!", "warning", 2000);
            $("#roleName").focus();
            return;
        }
        if (!/^[a-zA-Z0-9\s_-]+$/.test(name)) {
            showToast("Warning", "Role name contains invalid characters!", "warning", 2000);
            $("#roleName").focus();
            return;
        }

        const dto = JSON.stringify({ name });
        const ajaxOptions = {
            url: id ? `/api/roles/${id}` : "/api/roles",
            type: id ? "PUT" : "POST",
            contentType: "application/json",
            data: dto,
            timeout: 10000,
            success: () => {
                showToast("Success", `Role ${id ? "updated" : "created"} successfully`, "success", 4000);
                resetForm();
                fetchRoles();
            },
            error: (error) => {
                let message = parseErrorResponse(error);
                if (error.status === 0) {
                    message = "Network error or server is unreachable.";
                } else if (error.statusText === "timeout") {
                    message = "Request timed out. Please try again.";
                }
                showToast("Error", message, "danger", 2000);
            }
        };
        $.ajax(ajaxOptions);
    });

    $("#importForm").on("submit", function (e) {
        e.preventDefault();
        const formData = new FormData(this);
        $.ajax({
            url: "/api/roles/import",
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            timeout: 15000,
            success: () => {
                showToast("Success", "Roles imported successfully", "success", 4000);
                importModal.hide();
                fetchRoles();
            },
            error: (error) => {
                const message = parseErrorResponse(error);
                showToast("Error", message, "danger", 4000);
                $("#importForm").prepend(`
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        ${message}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                `);
            }
        });
    });

    $("#searchBtn").on("click", () => {
        searchQuery = $("#searchInput").val().trim();
        currentPage = 0;
        fetchRoles(currentPage, searchQuery);
    });

    $("#searchInput").on("keyup", function () {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            searchQuery = $(this).val().trim();
            currentPage = 0;
            fetchRoles(currentPage, searchQuery);
        }, 300);
    });

    $("#reloadBtn").on("click", () => {
        const reloadIcon = $("#reloadBtn i");
        reloadIcon.addClass("fa-spin");
        searchQuery = "";
        $("#searchInput").val("");
        currentPage = 0;
        fetchRoles()
            .then(() => {
                setTimeout(() => reloadIcon.removeClass("fa-spin"), 500);
                showToast("Success", "Data refreshed successfully", "success", 4000);
            })
            .catch(() => {
                reloadIcon.removeClass("fa-spin");
            });
    });

    $("#exportBtn").on("click", () => {
        exportRoles();
    });
});