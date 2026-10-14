/* Revitalle Head Spa - interações da landing (JS puro, sem dependências). */
(function () {
  'use strict';
  var doc = document.documentElement;
  var reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var finePointer = window.matchMedia('(hover: hover) and (pointer: fine)').matches;

  /* Refração do vidro: só o Chromium aplica filtro SVG em backdrop-filter. */
  var brands = navigator.userAgentData && navigator.userAgentData.brands;
  if (brands && brands.some(function (b) { return /Chromium/.test(b.brand); })) doc.classList.add('has-refraction');

  /* Cabeçalho muda ao rolar + parallax da foto do hero (um único rAF por quadro) */
  var heroImg = document.querySelector('[data-parallax]');
  var hero = document.getElementById('topo');
  var ticking = false;
  function onScroll() {
    if (ticking) return;
    ticking = true;
    requestAnimationFrame(function () {
      var y = window.scrollY || 0;
      doc.classList.toggle('is-scrolled', y > 40);
      if (heroImg && !reduceMotion && y < hero.offsetHeight) heroImg.style.setProperty('--py', (y * 0.28).toFixed(1) + 'px');
      ticking = false;
    });
  }
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  /* Menu no celular */
  var toggle = document.querySelector('[data-menu-toggle]');
  var nav = toggle && toggle.closest('.nav');
  if (toggle && nav) {
    var path = toggle.querySelector('path');
    var setMenu = function (open) {
      nav.classList.toggle('is-open', open);
      toggle.setAttribute('aria-expanded', String(open));
      toggle.setAttribute('aria-label', open ? 'Fechar menu' : 'Abrir menu');
      path.setAttribute('d', open ? 'M6 6l12 12M18 6L6 18' : 'M4 8h16M4 16h16');
    };
    toggle.addEventListener('click', function () { setMenu(toggle.getAttribute('aria-expanded') !== 'true'); });
    nav.addEventListener('click', function (e) { if (e.target.closest('.nav__menu a')) setMenu(false); });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && nav.classList.contains('is-open')) { setMenu(false); toggle.focus(); }
    });
  }

  /* Contadores (5,0 e 11) */
  function count(el) {
    var to = parseFloat(el.getAttribute('data-count'));
    var dec = parseInt(el.getAttribute('data-decimals') || '0', 10);
    var fmt = function (v) { return v.toFixed(dec).replace('.', ','); };
    if (reduceMotion) { el.textContent = fmt(to); return; }
    var t0 = performance.now(), dur = 1400;
    (function step(now) {
      var p = Math.min(1, (now - t0) / dur);
      el.textContent = fmt(to * (1 - Math.pow(1 - p, 3)));
      if (p < 1) requestAnimationFrame(step);
    })(t0);
  }

  /* Entradas no scroll, encadeadas por grupo (o conteúdo é visível sem JS) */
  var items = document.querySelectorAll('[data-reveal]');
  document.querySelectorAll('[data-reveal-group]').forEach(function (g) {
    g.querySelectorAll('[data-reveal]').forEach(function (el, i) { el.style.setProperty('--i', i); });
  });
  var counters = document.querySelectorAll('[data-count]');
  if ('IntersectionObserver' in window && !reduceMotion) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (!en.isIntersecting) return;
        en.target.classList.add('is-in');
        en.target.querySelectorAll('[data-count]').forEach(count);
        io.unobserve(en.target);
      });
    }, { rootMargin: '0px 0px -10% 0px', threshold: 0.12 });
    items.forEach(function (el) { io.observe(el); });
  } else {
    items.forEach(function (el) { el.classList.add('is-in'); });
    counters.forEach(count);
  }

  /* Reflexo do vidro acompanhando o ponteiro */
  if (finePointer && !reduceMotion) {
    var last = null;
    document.addEventListener('pointermove', function (e) {
      var g = e.target.closest && e.target.closest('.glass, .btn');
      if (last && last !== g) { last.style.removeProperty('--mx'); last.style.removeProperty('--my'); }
      last = g;
      if (!g) return;
      var r = g.getBoundingClientRect();
      g.style.setProperty('--mx', ((e.clientX - r.left) / r.width * 100).toFixed(1) + '%');
      g.style.setProperty('--my', ((e.clientY - r.top) / r.height * 100).toFixed(1) + '%');
    }, { passive: true });
  }

  /* Faixa contínua de depoimentos: duplica os cartões (cópia oculta de leitores de tela) para o loop sem emenda */
  document.querySelectorAll('[data-marquee]').forEach(function (m) {
    if (reduceMotion) return;
    var track = m.querySelector('.marquee__track');
    Array.prototype.slice.call(track.children).forEach(function (li) {
      var c = li.cloneNode(true);
      c.setAttribute('aria-hidden', 'true');
      track.appendChild(c);
    });
    m.style.setProperty('--marquee-dur', Math.round(track.scrollWidth / 2 / 40) + 's');
    m.classList.add('is-running');
  });

  /* WhatsApp flutuante some quando o bloco final (que já tem WhatsApp) está visível */
  var wa = document.querySelector('[data-wa-float]');
  var visit = document.getElementById('visite');
  if (wa && visit && 'IntersectionObserver' in window) {
    new IntersectionObserver(function (entries) {
      wa.classList.toggle('is-hidden', entries[0].isIntersecting);
    }, { threshold: 0.3 }).observe(visit);
  }
})();
