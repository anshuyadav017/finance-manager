function toJsonForm(form) {
    const fd = new FormData(form);
    const obj = {};
    for (const [k, v] of fd.entries()) obj[k] = v;
    return obj;
}

// Create Account
document.getElementById('createForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const data = toJsonForm(form);
    const body = new URLSearchParams(data).toString();
    
    const resultDiv = document.getElementById('createResult');
    showLoading(resultDiv);
    
    try {
        const res = await fetch('/api/create', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body
        });
        
        if (!res.ok) {
            const error = await res.json();
            showError(resultDiv, error.error || 'Failed to create account');
            return;
        }
        
        const account = await res.json();
        console.log('Account created:', account);
        
        if (account.id) {
            document.getElementById('accInput').value = account.id;
            document.getElementById('histAcc').value = account.id;
            showSuccess(resultDiv, `✓ Account created! ID: ${account.id}, Name: ${account.holderName}`);
            form.reset();
        } else {
            showError(resultDiv, 'Invalid response from server');
        }
    } catch (error) {
        console.error('Error:', error);
        showError(resultDiv, 'Failed to create account: ' + error.message);
    }
});

// Deposit
document.getElementById('depBtn').addEventListener('click', async () => {
    const acc = document.getElementById('accInput').value;
    const amount = document.getElementById('depAmount').value;
    const desc = document.getElementById('depDesc').value;
    
    const resultDiv = document.getElementById('opResult');
    
    if (!acc || !amount) {
        showError(resultDiv, 'Please enter account number and amount');
        return;
    }
    
    if (parseFloat(amount) <= 0) {
        showError(resultDiv, 'Amount must be greater than zero');
        return;
    }
    
    showLoading(resultDiv);
    const body = new URLSearchParams({ acc, amount, desc }).toString();
    
    try {
        const res = await fetch('/api/deposit', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body
        });
        
        if (!res.ok) {
            const error = await res.json();
            showError(resultDiv, error.error || 'Deposit failed');
            return;
        }
        
        const result = await res.json();
        console.log('Deposit result:', result);
        showSuccess(resultDiv, `✓ Deposited ₹${amount}. New Balance: ₹${result.newBalance.toFixed(2)}`);
        document.getElementById('depAmount').value = '';
        document.getElementById('depDesc').value = '';
        
        // Refresh balance
        const balanceEl = document.getElementById('balance');
        if (balanceEl) {
            balanceEl.textContent = result.newBalance.toFixed(2);
        }
    } catch (error) {
        console.error('Error:', error);
        showError(resultDiv, 'Deposit failed: ' + error.message);
    }
});

// Withdraw
document.getElementById('wdBtn').addEventListener('click', async () => {
    const acc = document.getElementById('accInput').value;
    const amount = document.getElementById('wdAmount').value;
    const desc = document.getElementById('wdDesc').value;
    
    const resultDiv = document.getElementById('opResult');
    
    if (!acc || !amount) {
        showError(resultDiv, 'Please enter account number and amount');
        return;
    }
    
    if (parseFloat(amount) <= 0) {
        showError(resultDiv, 'Amount must be greater than zero');
        return;
    }
    
    showLoading(resultDiv);
    const body = new URLSearchParams({ acc, amount, desc }).toString();
    
    try {
        const res = await fetch('/api/withdraw', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body
        });
        
        if (!res.ok) {
            const error = await res.json();
            showError(resultDiv, error.error || 'Withdrawal failed');
            return;
        }
        
        const result = await res.json();
        console.log('Withdraw result:', result);
        showSuccess(resultDiv, `✓ Withdrew ₹${amount}. New Balance: ₹${result.newBalance.toFixed(2)}`);
        document.getElementById('wdAmount').value = '';
        document.getElementById('wdDesc').value = '';
        
        // Refresh balance
        const balanceEl = document.getElementById('balance');
        if (balanceEl) {
            balanceEl.textContent = result.newBalance.toFixed(2);
        }
    } catch (error) {
        console.error('Error:', error);
        showError(resultDiv, 'Withdrawal failed: ' + error.message);
    }
});

// Get Balance
document.getElementById('getBal').addEventListener('click', async () => {
    const acc = document.getElementById('histAcc').value;
    const balanceEl = document.getElementById('balance');
    
    if (!acc) {
        showError(document.getElementById('history'), 'Please enter account number');
        return;
    }
    
    balanceEl.textContent = '...';
    balanceEl.classList.add('loading-pulse');
    
    try {
        const res = await fetch('/api/balance?acc=' + encodeURIComponent(acc));
        
        if (!res.ok) {
            const error = await res.json();
            throw new Error(error.error || 'Failed to fetch balance');
        }
        
        const balance = await res.text();
        balanceEl.textContent = parseFloat(balance).toFixed(2);
        balanceEl.classList.remove('loading-pulse');
        balanceEl.classList.add('balance-update');
        setTimeout(() => balanceEl.classList.remove('balance-update'), 1000);
    } catch (error) {
        console.error('Error:', error);
        balanceEl.textContent = '0.00';
        balanceEl.classList.remove('loading-pulse');
        showError(document.getElementById('history'), 'Failed to fetch balance: ' + error.message);
    }
});

// Get History
document.getElementById('getHist').addEventListener('click', async () => {
    const acc = document.getElementById('histAcc').value;
    const historyDiv = document.getElementById('history');
    
    if (!acc) {
        showError(historyDiv, 'Please enter account number');
        return;
    }
    
    showLoading(historyDiv);
    
    try {
        const res = await fetch('/api/history?acc=' + encodeURIComponent(acc));
        
        if (!res.ok) {
            const error = await res.json();
            throw new Error(error.error || 'Failed to fetch history');
        }
        
        const json = await res.json();
        
        if (!Array.isArray(json)) {
            showError(historyDiv, 'Invalid response from server');
            return;
        }
        
        if (json.length === 0) {
            showInfo(historyDiv, 'No transactions found for this account');
            return;
        }
        
        let html = '<div class="transaction-list">';
        json.forEach((t, index) => {
            const icon = t.type === 'deposit' ? 'fa-arrow-down' : 'fa-arrow-up';
            const typeClass = t.type === 'deposit' ? 'deposit' : 'withdraw';
            // Use timestamp field (try both timestamp and createdAt)
            const dateField = t.timestamp || t.createdAt;
            html += `
                <div class="transaction-item ${typeClass}" style="animation-delay: ${index * 0.1}s">
                    <div class="transaction-icon">
                        <i class="fas ${icon}"></i>
                    </div>
                    <div class="transaction-details">
                        <div class="transaction-header">
                            <span class="transaction-type">${t.type}</span>
                            <span class="transaction-amount">₹${parseFloat(t.amount).toFixed(2)}</span>
                        </div>
                        <div class="transaction-meta">
                            <span class="transaction-date"><i class="far fa-clock"></i> ${formatDate(dateField)}</span>
                            ${t.description ? `<span class="transaction-desc">${t.description}</span>` : ''}
                        </div>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        historyDiv.innerHTML = html;
    } catch (error) {
        console.error('Error:', error);
        showError(historyDiv, 'Failed to fetch transaction history: ' + error.message);
    }
});

// Utility Functions
function showLoading(element) {
    element.innerHTML = '<div class="loader"><div class="spinner"></div><span>Processing...</span></div>';
    element.className = 'result loading';
}

function showSuccess(element, message) {
    element.innerHTML = `<span>${message}</span>`;
    element.className = 'result success show';
    setTimeout(() => element.classList.remove('show'), 5000);
}

function showError(element, message) {
    element.innerHTML = `<span>${message}</span>`;
    element.className = 'result error show';
    setTimeout(() => element.classList.remove('show'), 5000);
}

function showInfo(element, message) {
    element.innerHTML = `<span>${message}</span>`;
    element.className = 'result info show';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', { 
        day: '2-digit', 
        month: 'short', 
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Parallax effect on mouse move
document.addEventListener('mousemove', (e) => {
    const cards = document.querySelectorAll('.glass-card');
    const x = e.clientX / window.innerWidth;
    const y = e.clientY / window.innerHeight;
    
    cards.forEach((card, index) => {
        const speed = (index + 1) * 2;
        const xShift = (x - 0.5) * speed;
        const yShift = (y - 0.5) * speed;
        card.style.transform = `translate(${xShift}px, ${yShift}px)`;
    });
});

// Animate elements on scroll
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('visible');
        }
    });
}, { threshold: 0.1 });

document.querySelectorAll('.slide-in, .slide-in-left, .slide-in-right').forEach(el => {
    observer.observe(el);
});

console.log('Finance App loaded and ready');
