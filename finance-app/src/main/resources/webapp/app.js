function toJsonForm(form) {
  const fd = new FormData(form);
  const obj = {};
  for (const [k,v] of fd.entries()) obj[k]=v;
  return obj;
}

document.getElementById('createForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const form = e.target;
  const data = toJsonForm(form);
  const body = new URLSearchParams(data).toString();
  const res = await fetch('/api/create', { method: 'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body });
  const txt = await res.text();
  document.getElementById('createResult').innerText = txt;
  // show created acc number
  try {
    const obj = JSON.parse(txt);
    document.getElementById('accInput').value = obj.accountNumber || '';
    document.getElementById('histAcc').value = obj.accountNumber || '';
  } catch(e){}
});

document.getElementById('depBtn').addEventListener('click', async () => {
  const acc = document.getElementById('accInput').value;
  const amount = document.getElementById('depAmount').value;
  const desc = document.getElementById('depDesc').value;
  const body = new URLSearchParams({ acc, amount, desc }).toString();
  const res = await fetch('/api/deposit', { method: 'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body });
  const txt = await res.text();
  document.getElementById('opResult').innerText = txt;
});

document.getElementById('wdBtn').addEventListener('click', async () => {
  const acc = document.getElementById('accInput').value;
  const amount = document.getElementById('wdAmount').value;
  const desc = document.getElementById('wdDesc').value;
  const body = new URLSearchParams({ acc, amount, desc }).toString();
  const res = await fetch('/api/withdraw', { method: 'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body });
  const txt = await res.text();
  document.getElementById('opResult').innerText = txt;
});

document.getElementById('getBal').addEventListener('click', async () => {
  const acc = document.getElementById('histAcc').value;
  const res = await fetch('/api/balance?acc=' + encodeURIComponent(acc));
  const txt = await res.text();
  document.getElementById('balance').innerText = txt;
});

document.getElementById('getHist').addEventListener('click', async () => {
  const acc = document.getElementById('histAcc').value;
  const res = await fetch('/api/history?acc=' + encodeURIComponent(acc));
  const json = await res.json();
  if (!Array.isArray(json)) {
    document.getElementById('history').innerText = JSON.stringify(json);
    return;
  }
  if (json.length === 0) {
    document.getElementById('history').innerText = 'No transactions';
    return;
  }
  let html = '<table><thead><tr><th>Date</th><th>Type</th><th>Amount</th><th>Balance</th><th>Description</th></tr></thead><tbody>';
  json.forEach(t => {
    html += `<tr><td>${t.createdAt}</td><td>${t.type}</td><td>${t.amount}</td><td>${t.balanceAfter}</td><td>${t.description||''}</td></tr>`;
  });
  html += '</tbody></table>';
  document.getElementById('history').innerHTML = html;
});

document.getElementById('getTopExp').addEventListener('click', async () => {
  const k = document.getElementById('topExpK').value;
  const res = await fetch('/api/top-expenses?k=' + encodeURIComponent(k));
  const json = await res.json();
  let html = '<ul>';
  json.forEach(item => {
    html += `<li>${item[1]}: ${item[0]}</li>`;
  });
  html += '</ul>';
  document.getElementById('topExpenses').innerHTML = html;
});

document.getElementById('getTopCat').addEventListener('click', async () => {
  const k = document.getElementById('topCatK').value;
  const res = await fetch('/api/top-categories?k=' + encodeURIComponent(k));
  const json = await res.json();
  let html = '<ul>';
  json.forEach(item => {
    html += `<li>${item[0]}: ${item[1]}</li>`;
  });
  html += '</ul>';
  document.getElementById('topCategories').innerHTML = html;
});

document.getElementById('getMonthlyAvg').addEventListener('click', async () => {
  const months = document.getElementById('monthlyMonths').value;
  const res = await fetch('/api/monthly-average?months=' + encodeURIComponent(months));
  const json = await res.json();
  document.getElementById('monthlyAverage').innerText = 'Monthly Average: ' + json.average;
});

document.getElementById('analyzeBudget').addEventListener('click', async () => {
  const budget = document.getElementById('budgetAmount').value;
  const res = await fetch('/api/budget-analysis?budget=' + encodeURIComponent(budget));
  const json = await res.json();
  document.getElementById('budgetAnalysis').innerText = json.analysis;
});

document.getElementById('undoBtn').addEventListener('click', async () => {
  const res = await fetch('/api/undo', { method: 'POST' });
  const json = await res.json();
  document.getElementById('undoResult').innerText = json.success ? 'Undo successful' : 'No transaction to undo';
});

document.getElementById('detectFraud').addEventListener('click', async () => {
  const res = await fetch('/api/fraud');
  const json = await res.json();
  let html = '<ul>';
  json.forEach(item => {
    html += `<li>${item}</li>`;
  });
  html += '</ul>';
  document.getElementById('fraudResult').innerHTML = html;
});

document.getElementById('getSuggestions').addEventListener('click', async () => {
  const prefix = document.getElementById('catPrefix').value;
  const res = await fetch('/api/suggestions?prefix=' + encodeURIComponent(prefix));
  const json = await res.json();
  let html = '<ul>';
  json.forEach(item => {
    html += `<li>${item}</li>`;
  });
  html += '</ul>';
  document.getElementById('suggestions').innerHTML = html;
});

document.getElementById('getAllTrans').addEventListener('click', async () => {
  const res = await fetch('/api/all-transactions');
  const json = await res.json();
  let html = '<table><thead><tr><th>ID</th><th>Type</th><th>Amount</th><th>Description</th><th>Date</th></tr></thead><tbody>';
  json.forEach(t => {
    html += `<tr><td>${t.id}</td><td>${t.type}</td><td>${t.amount}</td><td>${t.description||''}</td><td>${t.createdAt}</td></tr>`;
  });
  html += '</tbody></table>';
  document.getElementById('allTransactions').innerHTML = html;
});
