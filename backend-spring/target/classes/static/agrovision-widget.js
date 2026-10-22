/**
 * AgroVisión AI - Embedded Crop Diagnostics & Agrochemical Recommendation Widget
 * Designed for seamless embedding into Chiaway.com storefront
 * 
 * Usage:
 *   <link rel="stylesheet" href="agrovision-widget.css">
 *   <script src="agrovision-widget.js"></script>
 *   <script>
 *     AgrovisionWidget.init({
 *       apiUrl: 'http://localhost:8080'
 *     });
 *   </script>
 */

(function (window, document) {
  'use strict';

  var AgrovisionWidget = {
    config: {
      apiUrl: window.AGROVISION_API_URL || 'http://localhost:8080',
      themeColor: '#15803d',
      buttonText: 'Diagnóstico de Cultivo AI',
      targetContainer: null
    },

    state: {
      isOpen: false,
      selectedOrgan: 'leaf',
      selectedFile: null,
      previewUrl: null,
      plantHint: '',
      isSubmitting: false,
      diagnosisResult: null,
      selectedProductIds: [],
      clientLocation: null
    },

    init: function (options) {
      if (options) {
        for (var key in options) {
          if (options.hasOwnProperty(key)) {
            this.config[key] = options[key];
          }
        }
      }

      this.requestGeolocation();
      this.render();
      this.bindEvents();
    },

    requestGeolocation: function () {
      var self = this;
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          function (pos) {
            self.state.clientLocation = {
              lat: pos.coords.latitude,
              lon: pos.coords.longitude
            };
          },
          function (err) {
            // Permiso denegado o no disponible; backend usará coordenadas default de Perú
          },
          { timeout: 5000 }
        );
      }
    },

    render: function () {
      if (document.getElementById('cw-agrovision-root')) {
        return;
      }

      var root = document.createElement('div');
      root.id = 'cw-agrovision-root';

      // 1. Trigger button
      var triggerBtn = document.createElement('button');
      triggerBtn.className = 'cw-trigger-btn';
      triggerBtn.id = 'cw-trigger-btn';
      triggerBtn.setAttribute('aria-label', 'Abrir diagnóstico fitosanitario con IA');
      triggerBtn.innerHTML =
        '<div class="cw-pulse-badge"></div>' +
        '<svg viewBox="0 0 24 24"><path d="M17 8C8 10 5.9 16.17 3.82 21.34L5.71 22l1-2.3A4.49 4.49 0 0 0 8 20C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75C7 8 17 8 17 8z"/></svg>' +
        '<span>' + this.config.buttonText + '</span>';

      // 2. Modal backdrop
      var backdrop = document.createElement('div');
      backdrop.className = 'cw-modal-backdrop';
      backdrop.id = 'cw-modal-backdrop';

      backdrop.innerHTML =
        '<div class="cw-modal-box">' +
          '<div class="cw-header">' +
            '<div class="cw-header-brand">' +
              '<div class="cw-header-icon">' +
                '<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm1 14h-2v-2h2v2zm0-4h-2V7h2v5z"/></svg>' +
              '</div>' +
              '<div>' +
                '<h2 class="cw-header-title">AgroVisión AI</h2>' +
                '<p class="cw-header-subtitle">Diagnóstico fitosanitario y cotización Chiaway</p>' +
              '</div>' +
            '</div>' +
            '<button class="cw-close-btn" id="cw-close-btn" aria-label="Cerrar">&times;</button>' +
          '</div>' +
          '<div class="cw-body" id="cw-modal-body">' +
            // Content will be injected dynamically
          '</div>' +
        '</div>';

      root.appendChild(triggerBtn);
      root.appendChild(backdrop);
      document.body.appendChild(root);

      this.renderUploadView();
    },

    bindEvents: function () {
      var self = this;
      var triggerBtn = document.getElementById('cw-trigger-btn');
      var closeBtn = document.getElementById('cw-close-btn');
      var backdrop = document.getElementById('cw-modal-backdrop');

      if (triggerBtn) {
        triggerBtn.addEventListener('click', function () {
          self.openModal();
        });
      }

      if (closeBtn) {
        closeBtn.addEventListener('click', function () {
          self.closeModal();
        });
      }

      if (backdrop) {
        backdrop.addEventListener('click', function (e) {
          if (e.target === backdrop) {
            self.closeModal();
          }
        });
      }
    },

    openModal: function () {
      var backdrop = document.getElementById('cw-modal-backdrop');
      if (backdrop) {
        backdrop.classList.add('cw-active');
        this.state.isOpen = true;
      }
    },

    closeModal: function () {
      var backdrop = document.getElementById('cw-modal-backdrop');
      if (backdrop) {
        backdrop.classList.remove('cw-active');
        this.state.isOpen = false;
      }
    },

    renderUploadView: function () {
      var self = this;
      var body = document.getElementById('cw-modal-body');
      if (!body) return;

      var hasPreview = !!this.state.previewUrl;

      body.innerHTML =
        '<div id="cw-form-container">' +
          '<h3 class="cw-section-title">1. Seleccione la parte afectada</h3>' +
          '<div class="cw-organ-grid" id="cw-organ-grid">' +
            '<div class="cw-organ-pill ' + (this.state.selectedOrgan === 'leaf' ? 'cw-selected' : '') + '" data-organ="leaf">' +
              '<span class="cw-organ-icon">🍃</span><span>Hoja</span>' +
            '</div>' +
            '<div class="cw-organ-pill ' + (this.state.selectedOrgan === 'fruit' ? 'cw-selected' : '') + '" data-organ="fruit">' +
              '<span class="cw-organ-icon">🍇</span><span>Fruto</span>' +
            '</div>' +
            '<div class="cw-organ-pill ' + (this.state.selectedOrgan === 'flower' ? 'cw-selected' : '') + '" data-organ="flower">' +
              '<span class="cw-organ-icon">🌸</span><span>Flor</span>' +
            '</div>' +
            '<div class="cw-organ-pill ' + (this.state.selectedOrgan === 'bark' ? 'cw-selected' : '') + '" data-organ="bark">' +
              '<span class="cw-organ-icon">🪵</span><span>Tallo</span>' +
            '</div>' +
          '</div>' +

          '<div class="cw-input-group">' +
            '<label class="cw-label" for="cw-plant-hint">Cultivo (Opcional - ej: Vid, Palta, Arándano, Maíz, Papa):</label>' +
            '<input type="text" id="cw-plant-hint" class="cw-input" placeholder="Nombre común del cultivo" value="' + (this.state.plantHint || '') + '">' +
          '</div>' +

          '<h3 class="cw-section-title">2. Fotografía del cultivo</h3>' +
          (!hasPreview ?
            '<div class="cw-dropzone" id="cw-dropzone">' +
              '<input type="file" id="cw-file-input" accept="image/*" capture="environment">' +
              '<svg class="cw-dropzone-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">' +
                '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>' +
                '<polyline points="17 8 12 3 7 8"></polyline>' +
                '<line x1="12" y1="3" x2="12" y2="15"></line>' +
              '</svg>' +
              '<div class="cw-dropzone-text">Tome una foto o arrastre aquí su imagen</div>' +
              '<div class="cw-dropzone-subtext">JPG, PNG o WEBP (máximo 15MB). Buena luz y enfoque nítido.</div>' +
            '</div>'
            :
            '<div class="cw-preview-card">' +
              '<img src="' + this.state.previewUrl + '" class="cw-preview-img" alt="Vista previa de muestra">' +
              '<button type="button" class="cw-remove-btn" id="cw-remove-file" title="Eliminar foto">&times;</button>' +
            '</div>'
          ) +

          '<div id="cw-quality-alert"></div>' +

          '<button type="button" class="cw-primary-btn" id="cw-analyze-btn" ' + (!this.state.selectedFile ? 'disabled' : '') + '>' +
            '<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M9.5 3A6.5 6.5 0 0 1 16 9.5c0 1.61-.59 3.09-1.56 4.23l.27.27h.79l5 5-1.5 1.5-5-5v-.79l-.27-.27A6.516 6.516 0 0 1 9.5 16 6.5 6.5 0 0 1 3 9.5 6.5 6.5 0 0 1 9.5 3m0 2C7 5 5 7 5 9.5S7 14 9.5 14 14 12 14 9.5 12 5 9.5 5z"/></svg>' +
            '<span>Analizar Síntomas con IA</span>' +
          '</button>' +
        '</div>';

      // Bind organ selector
      var pills = body.querySelectorAll('.cw-organ-pill');
      pills.forEach(function (pill) {
        pill.addEventListener('click', function () {
          pills.forEach(function (p) { p.classList.remove('cw-selected'); });
          pill.classList.add('cw-selected');
          self.state.selectedOrgan = pill.getAttribute('data-organ');
        });
      });

      // Bind plant hint input
      var hintInput = document.getElementById('cw-plant-hint');
      if (hintInput) {
        hintInput.addEventListener('input', function (e) {
          self.state.plantHint = e.target.value;
        });
      }

      // Bind file upload & dropzone
      var fileInput = document.getElementById('cw-file-input');
      var dropzone = document.getElementById('cw-dropzone');
      var removeBtn = document.getElementById('cw-remove-file');
      var analyzeBtn = document.getElementById('cw-analyze-btn');

      if (fileInput) {
        fileInput.addEventListener('change', function (e) {
          if (e.target.files && e.target.files[0]) {
            self.handleFileSelect(e.target.files[0]);
          }
        });
      }

      if (dropzone) {
        ['dragenter', 'dragover'].forEach(function (eventName) {
          dropzone.addEventListener(eventName, function (e) {
            e.preventDefault();
            e.stopPropagation();
            dropzone.classList.add('cw-dragover');
          }, false);
        });

        ['dragleave', 'drop'].forEach(function (eventName) {
          dropzone.addEventListener(eventName, function (e) {
            e.preventDefault();
            e.stopPropagation();
            dropzone.classList.remove('cw-dragover');
          }, false);
        });

        dropzone.addEventListener('drop', function (e) {
          var dt = e.dataTransfer;
          var files = dt.files;
          if (files && files[0]) {
            self.handleFileSelect(files[0]);
          }
        });
      }

      if (removeBtn) {
        removeBtn.addEventListener('click', function () {
          self.state.selectedFile = null;
          self.state.previewUrl = null;
          self.renderUploadView();
        });
      }

      if (analyzeBtn) {
        analyzeBtn.addEventListener('click', function () {
          self.submitDiagnosis();
        });
      }
    },

    handleFileSelect: function (file) {
      if (!file.type.match('image.*')) {
        alert('Por favor seleccione un archivo de imagen (JPEG, PNG o WEBP).');
        return;
      }

      this.state.selectedFile = file;
      this.state.previewUrl = URL.createObjectURL(file);
      this.renderUploadView();
    },

    renderLoadingView: function (message) {
      var body = document.getElementById('cw-modal-body');
      if (!body) return;

      body.innerHTML =
        '<div class="cw-loading-container">' +
          '<div class="cw-spinner"></div>' +
          '<div class="cw-loading-title">' + (message || 'Procesando diagnóstico agrícola...') + '</div>' +
          '<div class="cw-loading-subtitle">Analizando imagen mediante Pl@ntNet v2, EPPO Global y OpenAI Vision...</div>' +
        '</div>';
    },

    submitDiagnosis: function () {
      var self = this;
      if (!this.state.selectedFile) return;

      this.renderLoadingView('Identificando especie y patógeno...');

      var formData = new FormData();
      formData.append('file', this.state.selectedFile);
      formData.append('organ', this.state.selectedOrgan || 'leaf');
      if (this.state.plantHint) {
        formData.append('plantHint', this.state.plantHint);
      }
      if (this.state.clientLocation) {
        formData.append('lat', this.state.clientLocation.lat);
        formData.append('lon', this.state.clientLocation.lon);
      }

      fetch(this.config.apiUrl + '/api/diagnosis', {
        method: 'POST',
        body: formData
      })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('Error en el servidor al procesar la imagen (Código ' + res.status + ')');
        }
        return res.json();
      })
      .then(function (data) {
        if (!data.qualityOk) {
          self.renderUploadView();
          var alertBox = document.getElementById('cw-quality-alert');
          if (alertBox) {
            alertBox.innerHTML =
              '<div class="cw-alert cw-alert-danger">' +
                '<strong>Calidad de imagen insuficiente:</strong> ' + (data.reason || 'Foto no nítida') +
                '<br><span style="margin-top:4px;display:block;">💡 <em>' + (data.tip || '') + '</em></span>' +
              '</div>';
          }
          return;
        }

        self.state.diagnosisResult = data;
        self.renderResultsView(data);
      })
      .catch(function (err) {
        self.renderUploadView();
        var alertBox = document.getElementById('cw-quality-alert');
        if (alertBox) {
          alertBox.innerHTML =
            '<div class="cw-alert cw-alert-danger">' +
              '<strong>Error de conexión:</strong> No se pudo conectar con el servidor AgroVisión (' + err.message + '). Verifique que el backend Spring Boot esté activo.' +
            '</div>';
        }
      });
    },

    renderResultsView: function (data) {
      var self = this;
      var body = document.getElementById('cw-modal-body');
      if (!body) return;

      var diag = data.diagnosis || {};
      var plant = data.plantIdentified || {};
      var treat = data.treatment || {};
      var products = (treat.products && treat.products.length > 0) ? treat.products : [];

      var urgency = (treat.urgency || 'media').toLowerCase();
      var urgencyBadge = 'cw-badge-' + urgency;

      var productsHtml = '';
      products.forEach(function (p) {
        productsHtml +=
          '<div class="cw-product-card">' +
            '<div class="cw-product-info">' +
              '<h4 class="cw-product-name">' + p.name + '</h4>' +
              '<div class="cw-product-sub">Ingrediente: <strong>' + p.activeIngredient + '</strong> &bull; ' + p.category + '</div>' +
              '<div class="cw-product-dose">Dosis: ' + (p.dose || 'Consultar etiqueta') + '</div>' +
              (p.description ? '<p style="font-size:12px;color:#475569;margin:6px 0 0 0;">' + p.description + '</p>' : '') +
            '</div>' +
            '<div class="cw-product-price">' + (p.price || 'A cotizar') + '</div>' +
          '</div>';
      });

      body.innerHTML =
        '<div id="cw-results-container">' +
          '<div class="cw-result-hero">' +
            '<div class="cw-badge-row">' +
              '<span class="cw-badge ' + urgencyBadge + '">Urgencia: ' + urgency.toUpperCase() + '</span>' +
              (diag.eppoCode ? '<span class="cw-badge cw-badge-info">EPPO: ' + diag.eppoCode + '</span>' : '') +
              '<span class="cw-badge cw-badge-info">Confianza: ' + Math.round((diag.confidence || 0.75) * 100) + '%</span>' +
              (data.needsExpert ? '<span class="cw-badge cw-badge-alta">Requiere validación de campo</span>' : '') +
            '</div>' +
            '<h3 class="cw-disease-title">' + (diag.diseaseName || 'Diagnóstico Fitopatológico') + '</h3>' +
            '<div class="cw-plant-sub">Cultivo identificado: <strong>' + (plant.species || 'Planta agrícola') + '</strong> ' +
              (plant.commonNames && plant.commonNames.length > 0 ? '(' + plant.commonNames.join(', ') + ')' : '') +
            '</div>' +
            '<div class="cw-recommendation-box">' +
              '<strong>Recomendación agronómica:</strong><br>' +
              (treat.recommendation || 'Aplicar control fitosanitario preventivo/curativo siguiendo buenas prácticas agrícolas.') +
              '<div style="font-size:12px;color:#64748b;margin-top:6px;">Monitoreo sugerido: re-evaluar en ' + (treat.followUpDays || 7) + ' días.</div>' +
            '</div>' +
          '</div>' +

          '<h3 class="cw-section-title">Productos recomendados en Chiaway</h3>' +
          (products.length > 0 ? productsHtml : '<p style="font-size:13px;color:#64748b;">No se encontraron productos específicos registrados para este patógeno.</p>') +

          '<div class="cw-quote-box">' +
            '<h3 style="font-size:15px;font-weight:700;margin:0 0 10px 0;color:#0f172a;">Solicitar Cotización y Despacho en Chiaway</h3>' +
            '<p style="font-size:12px;color:#475569;margin:0 0 14px 0;">Envíanos tus datos para coordinar la entrega o recojo de los insumos recomendados con nuestro equipo técnico.</p>' +
            '<div class="cw-form-row">' +
              '<div>' +
                '<label class="cw-label">Nombre o Fundo *</label>' +
                '<input type="text" id="cw-quote-name" class="cw-input" placeholder="Ej: Agrícola El Sol / Juan Pérez" required>' +
              '</div>' +
              '<div>' +
                '<label class="cw-label">Teléfono / WhatsApp *</label>' +
                '<input type="tel" id="cw-quote-phone" class="cw-input" placeholder="Ej: +51 987 654 321" required>' +
              '</div>' +
            '</div>' +
            '<div class="cw-input-group">' +
              '<label class="cw-label">Correo electrónico</label>' +
              '<input type="email" id="cw-quote-email" class="cw-input" placeholder="ejemplo@chiaway.com">' +
            '</div>' +
            '<div class="cw-input-group">' +
              '<label class="cw-label">Hectáreas o notas adicionales</label>' +
              '<input type="text" id="cw-quote-notes" class="cw-input" placeholder="Ej: 5 ha de vid en Ica">' +
            '</div>' +
            '<button type="button" class="cw-primary-btn" id="cw-submit-quote-btn">' +
              'Enviar Solicitud de Cotización' +
            '</button>' +
          '</div>' +

          '<button type="button" class="cw-secondary-btn" id="cw-new-analysis-btn">Realizar nuevo diagnóstico</button>' +
        '</div>';

      // Bind actions
      var newBtn = document.getElementById('cw-new-analysis-btn');
      if (newBtn) {
        newBtn.addEventListener('click', function () {
          self.state.selectedFile = null;
          self.state.previewUrl = null;
          self.state.diagnosisResult = null;
          self.renderUploadView();
        });
      }

      var quoteBtn = document.getElementById('cw-submit-quote-btn');
      if (quoteBtn) {
        quoteBtn.addEventListener('click', function () {
          self.submitQuote(data);
        });
      }
    },

    submitQuote: function (diagData) {
      var self = this;
      var name = document.getElementById('cw-quote-name').value.trim();
      var phone = document.getElementById('cw-quote-phone').value.trim();
      var email = document.getElementById('cw-quote-email').value.trim();
      var notes = document.getElementById('cw-quote-notes').value.trim();

      if (!name || !phone) {
        alert('Por favor ingrese al menos su nombre y teléfono de contacto.');
        return;
      }

      var treat = diagData.treatment || {};
      var prods = (treat.products || []).map(function (p) {
        return {
          id: p.id,
          name: p.name,
          dose: p.dose,
          price: p.price
        };
      });

      var payload = {
        diagnosisId: diagData.diagnosisId,
        clientName: name,
        clientPhone: phone,
        clientEmail: email,
        notes: notes,
        products: prods
      };

      var quoteBtn = document.getElementById('cw-submit-quote-btn');
      if (quoteBtn) {
        quoteBtn.disabled = true;
        quoteBtn.innerText = 'Enviando solicitud...';
      }

      fetch(this.config.apiUrl + '/api/quotes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      .then(function (res) {
        if (!res.ok) throw new Error('Error al enviar cotización');
        return res.json();
      })
      .then(function (savedQuote) {
        var body = document.getElementById('cw-modal-body');
        if (body) {
          body.innerHTML =
            '<div style="text-align:center;padding:30px 10px;">' +
              '<div style="width:60px;height:60px;background:#dcfce7;color:#15803d;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 16px;">' +
                '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg>' +
              '</div>' +
              '<h3 style="font-size:20px;font-weight:700;color:#0f172a;margin-bottom:8px;">¡Solicitud de Cotización Recibida!</h3>' +
              '<p style="font-size:14px;color:#475569;max-width:440px;margin:0 auto 16px;">' +
                'Hemos registrado su requerimiento con código <strong>' + (savedQuote.id || 'COTIZACION') + '</strong>. Un asesor técnico de Chiaway se comunicará al <strong>' + phone + '</strong> con los precios especiales y opciones de despacho.' +
              '</p>' +
              '<button type="button" class="cw-primary-btn" id="cw-finish-btn" style="max-width:280px;margin:0 auto;">Cerrar y Volver a la Tienda</button>' +
            '</div>';

          document.getElementById('cw-finish-btn').addEventListener('click', function () {
            self.closeModal();
            self.state.selectedFile = null;
            self.state.previewUrl = null;
            self.state.diagnosisResult = null;
            self.renderUploadView();
          });
        }
      })
      .catch(function (err) {
        alert('No se pudo enviar la cotización: ' + err.message);
        if (quoteBtn) {
          quoteBtn.disabled = false;
          quoteBtn.innerText = 'Enviar Solicitud de Cotización';
        }
      });
    }
  };

  // Expose to global namespace
  window.AgrovisionWidget = AgrovisionWidget;

  // Auto-init on DOMContentLoaded if configured
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      AgrovisionWidget.init();
    });
  } else {
    AgrovisionWidget.init();
  }

})(window, document);
