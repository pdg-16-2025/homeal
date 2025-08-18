// Smooth scrolling for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        
        const targetId = this.getAttribute('href').substring(1);
        const targetSection = document.getElementById(targetId);
        
        if (targetSection) {
            const offsetTop = targetSection.offsetTop;
            
            window.scrollTo({
                top: offsetTop,
                behavior: 'smooth'
            });
        }
    });
});

// Intersection Observer for animations
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver(function(entries) {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe all animated elements
document.addEventListener('DOMContentLoaded', function() {
    const animatedElements = document.querySelectorAll('.feature-card, .step, .benefit-item, .custom-card');
    
    animatedElements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        element.style.transition = 'all 0.6s ease-out';
        observer.observe(element);
    });
});

// Phone demo animation
document.addEventListener('DOMContentLoaded', function() {
    const ingredients = [
        { icon: '🥕', name: 'Carrots', expiry: 'Expires in 3 days' },
        { icon: '🥛', name: 'Milk', expiry: 'Fresh for 5 days' },
        { icon: '🍗', name: 'Chicken Breast', expiry: 'Use today' },
        { icon: '🥬', name: 'Lettuce', expiry: 'Expires in 2 days' },
        { icon: '🍅', name: 'Tomatoes', expiry: 'Fresh for 4 days' },
        { icon: '🧀', name: 'Cheese', expiry: 'Expires in 1 week' }
    ];
    
    const ingredientsContainer = document.querySelector('.demo-ingredients');
    let currentIndex = 0;
    
    function updateIngredients() {
        if (ingredientsContainer) {
            ingredientsContainer.innerHTML = '';
            
            for (let i = 0; i < 3; i++) {
                const ingredient = ingredients[(currentIndex + i) % ingredients.length];
                const ingredientElement = document.createElement('div');
                ingredientElement.className = 'ingredient-item';
                ingredientElement.style.opacity = '0';
                ingredientElement.style.transform = 'translateX(30px)';
                
                ingredientElement.innerHTML = `
                    <div class="ingredient-icon">${ingredient.icon}</div>
                    <div class="ingredient-info">
                        <div class="ingredient-name">${ingredient.name}</div>
                        <div class="ingredient-expiry">${ingredient.expiry}</div>
                    </div>
                `;
                
                ingredientsContainer.appendChild(ingredientElement);
                
                // Animate in
                setTimeout(() => {
                    ingredientElement.style.opacity = '1';
                    ingredientElement.style.transform = 'translateX(0)';
                    ingredientElement.style.transition = 'all 0.3s ease-out';
                }, i * 100);
            }
            
            currentIndex = (currentIndex + 1) % ingredients.length;
        }
    }
    
    // Initial load
    updateIngredients();
    
    // Update every 4 seconds
    setInterval(updateIngredients, 4000);
});

// Counter animation for stats
function animateCounters() {
    const counters = document.querySelectorAll('.stat-value');
    
    counters.forEach(counter => {
        const target = counter.textContent;
        const isPercentage = target.includes('%');
        const isCurrency = target.includes('€');
        const isTime = target.includes('hr');
        const isNumber = !isPercentage && !isCurrency && !isTime && !target.includes('+');
        
        let endValue;
        if (isPercentage) {
            endValue = parseInt(target);
        } else if (isCurrency) {
            endValue = parseInt(target.replace('€', ''));
        } else if (isTime) {
            endValue = parseInt(target);
        } else if (target.includes('+')) {
            endValue = parseInt(target.replace('+', ''));
        } else {
            endValue = parseInt(target) || 0;
        }
        
        let currentValue = 0;
        const increment = endValue / 50;
        const timer = setInterval(() => {
            currentValue += increment;
            if (currentValue >= endValue) {
                currentValue = endValue;
                clearInterval(timer);
            }
            
            let displayValue = Math.floor(currentValue);
            if (isPercentage) {
                counter.textContent = displayValue + '%';
            } else if (isCurrency) {
                counter.textContent = '€' + displayValue + '+';
            } else if (isTime) {
                counter.textContent = displayValue + 'hrs';
            } else if (target.includes('+')) {
                counter.textContent = displayValue + '+';
            } else {
                counter.textContent = displayValue;
            }
        }, 30);
    });
}

// Trigger counter animation when stats section is visible
const statsObserver = new IntersectionObserver(function(entries) {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            animateCounters();
            statsObserver.unobserve(entry.target);
        }
    });
}, { threshold: 0.5 });

document.addEventListener('DOMContentLoaded', function() {
    const statsCard = document.querySelector('.stats-card');
    if (statsCard) {
        statsObserver.observe(statsCard);
    }
});

// Form validation and interaction (if needed in the future)
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// Add loading states to buttons
document.querySelectorAll('.btn-primary, .btn-secondary').forEach(button => {
    button.addEventListener('click', function(e) {
        if (this.getAttribute('href') === '#' || this.getAttribute('href') === '#download') {
            e.preventDefault();
            
            // Add loading state
            const originalText = this.innerHTML;
            this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Coming Soon...';
            this.style.opacity = '0.7';
            this.style.pointerEvents = 'none';
            
            // Reset after 2 seconds
            setTimeout(() => {
                this.innerHTML = originalText;
                this.style.opacity = '1';
                this.style.pointerEvents = 'auto';
            }, 2000);
        }
    });
});

// Add parallax effect to hero section
window.addEventListener('scroll', function() {
    const scrolled = window.pageYOffset;
    const heroImage = document.querySelector('.hero-image');
    
    if (heroImage && scrolled < window.innerHeight) {
        const rate = scrolled * -0.5;
        heroImage.style.transform = `translateY(${rate}px)`;
    }
});

// Add hover effects to feature cards
document.querySelectorAll('.feature-card').forEach(card => {
    card.addEventListener('mouseenter', function() {
        this.style.transform = 'translateY(-10px) scale(1.02)';
    });
    
    card.addEventListener('mouseleave', function() {
        this.style.transform = 'translateY(0) scale(1)';
    });
});

// Performance optimization: Lazy load images if any are added
if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                imageObserver.unobserve(img);
            }
        });
    });

    document.querySelectorAll('img[data-src]').forEach(img => {
        imageObserver.observe(img);
    });
}