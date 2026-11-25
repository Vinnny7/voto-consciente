'use strict';

const get = (id) => document.getElementById(id);
const keys = [...document.querySelectorAll('[data-key]')];
let session = new Urna.SessaoVotacao();

function initials(name) {
  const words = name.split(' ');
  return words[0][0] + words.at(-1)[0];
}

function renderOffice() {
  const office = session.office;
  get('ballot').hidden = session.ended;
  get('finish').hidden = !session.ended;
  get('device-label').textContent = session.ended ? 'Concluída' : 'Em votação';
  get('candidate-list').hidden = session.ended;

  for (const id of ['deputado', 'presidente']) {
    const step = get('step-' + id);
    const active = office?.id === id;
    const completed = session.ended || (id === 'deputado' && office?.id === 'presidente');
    step.classList.toggle('complete', completed);
    if (active) step.setAttribute('aria-current', 'step');
    else step.removeAttribute('aria-current');
  }

  if (session.ended) {
    get('candidate-office').textContent = 'Votação concluída';
    get('candidate-note').textContent = 'Use “Nova votação” para liberar a urna para a próxima pessoa.';
  } else {
    get('office-title').textContent = office.title;
    get('candidate-office').textContent = office.title;
    get('candidate-note').textContent = 'Escolha um número da lista e digite no teclado da urna.';
    get('candidate-list').innerHTML = office.candidates.map((candidate) => `
      <li>
        <span class="avatar ${candidate.color}" aria-hidden="true">${initials(candidate.name)}</span>
        <div><strong>${candidate.name}</strong><span>${candidate.party}</span></div>
        <b class="candidate-number">${candidate.number}</b>
      </li>`).join('');
  }

  renderInput();
}

function renderInput() {
  for (const key of keys) key.disabled = session.ended || session.blank || session.canConfirm;
  get('white-key').disabled = session.ended || session.blank;
  get('correct-key').disabled = session.ended || (!session.digits && !session.blank);
  get('confirm-key').disabled = !session.canConfirm;
  if (session.ended) return;

  const digitBoxes = get('digits');
  digitBoxes.hidden = session.blank;
  digitBoxes.innerHTML = Array.from({ length: session.office.digits }, (_, index) => {
    const value = session.digits[index] ?? '';
    const active = index === session.digits.length && !session.blank;
    return `<span class="digit${active ? ' active' : ''}">${value}</span>`;
  }).join('');

  const preview = get('candidate-preview');
  const hint = get('screen-hint');
  if (session.blank) {
    preview.innerHTML = '<strong>Voto em branco</strong>';
    hint.textContent = 'Confirme o voto em branco ou aperte Corrige.';
  } else if (session.candidate) {
    const { name, party } = session.candidate;
    preview.innerHTML = `<span class="sr-only">Número ${session.digits}. </span><strong>${name}</strong><span>${party}</span>`;
    hint.textContent = 'Confira o nome e aperte Confirma ou Corrige.';
  } else if (session.canConfirm) {
    preview.innerHTML = `<span class="sr-only">Número ${session.digits}. </span><strong>Voto nulo</strong><span>Número sem candidato cadastrado.</span>`;
    hint.textContent = 'Confirme o voto nulo ou aperte Corrige.';
  } else {
    const count = session.digits.length;
    preview.innerHTML = count ? `<span class="sr-only">Número digitado até agora: ${session.digits}</span>` : '';
    hint.textContent = `Digite os ${session.office.digits} dígitos do candidato.`;
  }
}

function enterDigit(digit) {
  if (!session.enterDigit(digit)) return;
  renderInput();
  if (session.canConfirm) get('confirm-key').focus({ preventScroll: true });
}

function selectBlank() {
  if (!session.selectBlank()) return;
  renderInput();
  get('confirm-key').focus({ preventScroll: true });
}

function correct() {
  if (!session.correct()) return;
  renderInput();
  get('office-title').focus({ preventScroll: true });
}

function confirm() {
  if (!session.confirm()) return;
  renderOffice();
  get(session.ended ? 'finish-title' : 'office-title').focus({ preventScroll: true });
}

get('keypad').addEventListener('click', (event) => {
  const button = event.target.closest('[data-key]');
  if (button) enterDigit(button.dataset.key);
});
get('white-key').addEventListener('click', selectBlank);
get('correct-key').addEventListener('click', correct);
get('confirm-key').addEventListener('click', confirm);
get('restart').addEventListener('click', () => {
  session = new Urna.SessaoVotacao();
  renderOffice();
  get('office-title').focus({ preventScroll: true });
});

document.addEventListener('keydown', (event) => {
  if (event.ctrlKey || event.metaKey || event.altKey || session.ended) return;
  const target = event.target;
  if (target.closest('input, textarea, select, [contenteditable="true"]')) return;
  if (!target.closest('#simulador') && target !== document.body) return;
  if (/^[0-9]$/.test(event.key)) {
    event.preventDefault();
    enterDigit(event.key);
  } else if (event.key === 'Backspace') {
    event.preventDefault();
    correct();
  } else if (event.key === 'Enter' && !target.closest('button, a')) {
    event.preventDefault();
    confirm();
  }
});

get('answers').addEventListener('click', (event) => {
  const option = event.target.closest('[data-answer]');
  if (!option) return;
  const correctAnswer = option.dataset.answer === 'check';
  const feedback = get('answer-feedback');
  feedback.textContent = correctAnswer
    ? 'Isso! Verificar a informação em uma fonte oficial ajuda a evitar que um boato se espalhe.'
    : 'Antes de repassar, confirme a informação em um canal oficial. Um vídeo sem fonte pode estar fora de contexto ou ter sido manipulado.';
  feedback.classList.toggle('try-again', !correctAnswer);
  feedback.hidden = false;
});

renderOffice();
