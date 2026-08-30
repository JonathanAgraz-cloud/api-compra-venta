// Dashboard de oportunidades: consume GET /api/opportunities (protegido por
// HTTP Basic -- el navegador ya maneja el prompt de autenticacion) y pinta
// la tabla. Vanilla JS, sin dependencias externas.
(function () {
  'use strict';

  var estadoCarga = document.getElementById('estado-carga');
  var estadoError = document.getElementById('estado-error');
  var estadoVacio = document.getElementById('estado-vacio');
  var tablaWrapper = document.getElementById('tabla-wrapper');
  var tablaCuerpo = document.getElementById('tabla-cuerpo');
  var resumen = document.getElementById('resumen');
  var resumenTotal = document.getElementById('resumen-total');
  var resumenAlta = document.getElementById('resumen-alta');

  var formatoMoneda = new Intl.NumberFormat('es-MX', {
    style: 'currency',
    currency: 'MXN',
    maximumFractionDigits: 0
  });
  var formatoFecha = new Intl.DateTimeFormat('es-MX', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  });

  var ZONA_ETIQUETAS = {
    ALTABRISA: 'Altabrisa',
    TEMOZON_NORTE: 'Temozón Norte',
    CHOLUL: 'Cholul',
    DZITYA: 'Dzityá',
    YUCATAN_COUNTRY_CLUB: 'Yucatán Country Club',
    FRANCISCO_DE_MONTEJO: 'Francisco de Montejo',
    OTRA_ZONA: 'Otra zona'
  };

  var CLASIFICACION_ETIQUETAS = {
    ALTA: 'Alta',
    MEDIA: 'Media',
    BAJA: 'Baja'
  };

  // El texto scrapeado (titulo del anuncio) se trata siempre como dato no
  // confiable: se inserta via textContent, nunca como HTML crudo.
  function escaparTexto(texto) {
    var contenedor = document.createElement('div');
    contenedor.textContent = texto == null ? '' : String(texto);
    return contenedor.innerHTML;
  }

  // La URL tambien viene del scraper: se valida el esquema y se neutralizan
  // comillas antes de usarla como atributo href.
  function urlSegura(url) {
    if (typeof url !== 'string' || !/^https:\/\//i.test(url)) {
      return '#';
    }
    return url.replace(/"/g, '%22');
  }

  function mostrarEstado(elementoVisible) {
    [estadoCarga, estadoError, estadoVacio, tablaWrapper].forEach(function (el) {
      el.hidden = el !== elementoVisible;
    });
  }

  function crearCelda(html, etiqueta) {
    var td = document.createElement('td');
    td.dataset.etiqueta = etiqueta;
    td.innerHTML = html;
    return td;
  }

  function construirFila(oportunidad) {
    var tr = document.createElement('tr');

    var imagenHtml = oportunidad.imagenUrl
      ? '<img class="anuncio-img" src="' + urlSegura(oportunidad.imagenUrl).replace('#', '') + '" alt="" loading="lazy" onerror="this.remove()">'
      : '<div class="anuncio-img"></div>';
    tr.appendChild(crearCelda(imagenHtml, ''));

    var tituloHtml = '<span class="anuncio-titulo"><a href="' + urlSegura(oportunidad.url) +
      '" target="_blank" rel="noopener noreferrer">' + escaparTexto(oportunidad.tituloAnuncio) + '</a></span>' +
      (oportunidad.enviadoExitosamente ? '' : '<span class="no-enviado">No se pudo enviar por Telegram</span>');
    tr.appendChild(crearCelda(tituloHtml, 'Anuncio'));

    tr.appendChild(crearCelda(escaparTexto(ZONA_ETIQUETAS[oportunidad.zona] || oportunidad.zona), 'Zona'));
    tr.appendChild(crearCelda(formatoMoneda.format(oportunidad.precioCompra), 'Compra'));
    tr.appendChild(crearCelda(formatoMoneda.format(oportunidad.precioReventaEstimado), 'Reventa est.'));
    tr.appendChild(crearCelda(
      '<span class="precio-ganancia">' + formatoMoneda.format(oportunidad.gananciaEstimada) + '</span>',
      'Ganancia'
    ));

    var clasificacionClase = (oportunidad.clasificacion || '').toLowerCase();
    var clasificacionTexto = CLASIFICACION_ETIQUETAS[oportunidad.clasificacion] || oportunidad.clasificacion;
    tr.appendChild(crearCelda(
      '<span class="badge badge--' + clasificacionClase + '">' + escaparTexto(clasificacionTexto) + '</span>',
      'Clasificación'
    ));

    var fechaTexto = oportunidad.fechaEnvio ? formatoFecha.format(new Date(oportunidad.fechaEnvio)) : '—';
    tr.appendChild(crearCelda(fechaTexto, 'Detectado'));

    return tr;
  }

  function actualizarResumen(oportunidades) {
    resumenTotal.textContent = String(oportunidades.length);
    resumenAlta.textContent = String(oportunidades.filter(function (o) {
      return o.clasificacion === 'ALTA';
    }).length);
    resumen.hidden = oportunidades.length === 0;
  }

  fetch('/api/opportunities', { headers: { Accept: 'application/json' } })
    .then(function (respuesta) {
      if (!respuesta.ok) {
        throw new Error('el servidor respondió con estado ' + respuesta.status);
      }
      return respuesta.json();
    })
    .then(function (oportunidades) {
      actualizarResumen(oportunidades);

      if (oportunidades.length === 0) {
        mostrarEstado(estadoVacio);
        return;
      }

      tablaCuerpo.innerHTML = '';
      oportunidades.forEach(function (oportunidad) {
        tablaCuerpo.appendChild(construirFila(oportunidad));
      });
      mostrarEstado(tablaWrapper);
    })
    .catch(function (error) {
      estadoError.textContent = 'No se pudieron cargar las oportunidades (' + error.message + '). Intenta recargar la página.';
      mostrarEstado(estadoError);
    });
})();
