// Handle logout event
document.addEventListener('DOMContentLoaded', function () {
    // Handle logout logic
    const logoutLinks = document.querySelectorAll('.logout-link');

    logoutLinks.forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();

            // Send POST request to logout API
            fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => {
                    console.log(response);
                    if (response.ok) {
                        window.location.href = '/login';
                    } else {
                        console.error('Logout failed');
                    }
                })
                .catch(error => {
                    console.error('Error during logout:', error);
                });
        });
    });

    // Handle login logic
    initLoginHandlers();

    // Xử lý sự kiện click vào các link chuyển đổi role
    document.querySelectorAll('.switch-role-link').forEach(link => {
        link.addEventListener('click', async function(e) {
            e.preventDefault();
            const newRole = this.getAttribute('data-role');
            console.log('newRole: ', newRole);
            try {
                const response = await fetch('/api/auth/switch-role', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ newRole: newRole })
                });

                if (!response.ok) {
                    throw new Error('Failed to switch role');
                }

                const data = await response.json();
                console.log('newToken: ', data.data);
                
                // Redirect to dashboard
                window.location.href = '/';
            } catch (error) {
                console.error('Error switching role:', error);
                alert('Failed to switch role. Please try again.');
            }
        });
    });
});

// Show logout message if URL has ?logout
function initLoginHandlers() {
    // Only handle when on login page
    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;

    // Check if there's a logout message
    const params = new URLSearchParams(window.location.search);
    if (params.has('logout')) {
        const alertLogout = document.getElementById('alertLogout');
        if (alertLogout) {
            alertLogout.style.display = 'block';
        }
    }

    // Handle login form submission
    loginForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        const alertError = document.getElementById('alertError');
        if (alertError) {
            alertError.style.display = 'none';
        }

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const rememberMe = document.getElementById('rememberMe').checked;

        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ username, password, rememberMe })
            });

            if (!res.ok) throw new Error('Unauthorized');
            window.location.href = '/';  // REDIRECT TO HOME PAGE
        } catch (err) {
            if (alertError) {
                alertError.style.display = 'block';
            }
        }
    });
}