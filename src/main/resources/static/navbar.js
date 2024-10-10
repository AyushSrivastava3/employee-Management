
document.addEventListener("DOMContentLoaded", function () {
    // Fetch the navbar HTML
    fetch("navbar.html")
      .then((response) => response.text())
      .then((data) => {
        document.getElementById("navbar").innerHTML = data;
  
        // Check the user's login state from local storage
        const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        const username = localStorage.getItem('username');
  
        const loginRegisterLink = document.querySelector('a[href="login.html"]');
        const profileDropdown = document.getElementById('profileDropdown');
        const navbarUsername = document.getElementById('navbarUsername');
        const navbarUsernameInitial = document.getElementById('navbarUsernameInitial');
        const dropdownUsernameDisplay = document.getElementById('dropdownUsernameDisplay');
        const dropdownUsernameInitial = document.getElementById('dropdownUsernameInitial');
        const dropdownMenu = document.getElementById('dropdownMenu');
  
        // If user is logged in via token
        if (isLoggedIn && username) {
          // Update navbar to show the username and the first letter in the circular placeholder
          navbarUsername.textContent = username;
          navbarUsernameInitial.textContent = username.charAt(0).toUpperCase();
          dropdownUsernameDisplay.textContent = username;
          dropdownUsernameInitial.textContent = username.charAt(0).toUpperCase();
  
          // Hide login/register and show profile dropdown
          loginRegisterLink.style.display = 'none';
          profileDropdown.classList.remove('hidden');
        } else {
          // If not logged in via token, fetch user info from session
          fetch("http://localhost:8080/user/current-user")
            .then((response) => {
              if (response.ok) {
                return response.json();
              } else {
                throw new Error('User not logged in');
              }
            })
            .then((user) => {
              const username = user.username;
  
              // Update navbar to show the username and the first letter in the circular placeholder
              navbarUsername.textContent = username;
              navbarUsernameInitial.textContent = username.charAt(0).toUpperCase();
              dropdownUsernameDisplay.textContent = username;
              dropdownUsernameInitial.textContent = username.charAt(0).toUpperCase();
  
              // Hide login/register and show profile dropdown
              loginRegisterLink.style.display = 'none';
              profileDropdown.classList.remove('hidden');
            })
            .catch((error) => {
              // If user is not logged in, show login/register link
              loginRegisterLink.style.display = 'block';
              profileDropdown.classList.add('hidden');
            });
        }
  
  
          // Handle logout
          const logoutButton = document.getElementById('logoutButton');
          if (logoutButton) {
              logoutButton.addEventListener('click', function () {
                  // Clear local storage on logout
                  localStorage.removeItem('token');
                  localStorage.removeItem('isLoggedIn');
                  localStorage.removeItem('username');
  
                  // Invalidate OAuth2 session by hitting the logout endpoint
                  fetch('http://localhost:8080/user/logout', {
                      method: 'POST', // Assuming your logout endpoint is a POST request
                      credentials: 'include' // Include cookies for session management
                  })
                  .then(response => {
                      if (response.ok) {
                          console.log('Logged out successfully from OAuth2 session.');
                          // Redirect to the home page or login page
                          window.location.href = 'index.html';
                      } else {
                          console.error('Failed to log out from OAuth2 session.');
                          // Handle logout failure if necessary
                          window.location.href = 'index.html'; // Redirect to home
                      }
                  })
                  .catch(error => {
                      console.error('Error during logout:', error);
                      // In case of error, redirect to home
                      window.location.href = 'index.html';
                  });
              });
          }
  
  
        // Toggle dropdown visibility on profile button click
        const authButton = document.getElementById('authButton');
        if (authButton) {
          authButton.addEventListener('click', function () {
            dropdownMenu.classList.toggle('hidden');
          });
        }
      });
  });
  