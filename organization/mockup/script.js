// Simple navigation functionality
const navItems = document.querySelectorAll('.nav-item');
const pages = document.querySelectorAll('.page');
const pageTitle = document.getElementById('page-title');

// Page titles mapping
const pageTitles = {
    'calendar': 'Menu Calendar',
    'fridge': 'Fridge',
    'shopping': 'Shopping List',
    'settings': 'Settings'
};

// Navigation functionality
function switchPage(pageName) {
    // Update navigation
    navItems.forEach(item => {
        item.classList.remove('active');
        if (item.dataset.page === pageName) {
            item.classList.add('active');
        }
    });

    // Update pages
    pages.forEach(page => {
        page.classList.remove('active');
    });

    const targetPage = document.getElementById(`${pageName}-page`);
    if (targetPage) {
        targetPage.classList.add('active');
        // Update page title
        pageTitle.textContent = pageTitles[pageName] || 'HomEAL';
    }
}

// Bottom navigation event listeners
navItems.forEach(item => {
    item.addEventListener('click', () => {
        const pageName = item.dataset.page;
        switchPage(pageName);
    });
});

// Simple category filtering for fridge (visual only)
document.querySelectorAll('.category-tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.category-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');

        const category = tab.dataset.category;
        const items = document.querySelectorAll('.item-card');

        items.forEach(item => {
            if (category === 'all' || item.dataset.category === category) {
                item.style.display = 'flex';
            } else {
                item.style.display = 'none';
            }
        });
    });
});

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    // Set initial page
    switchPage('calendar');
});

