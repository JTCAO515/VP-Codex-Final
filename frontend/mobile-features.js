// mobile-features.js — v4.0 Mobile AI capabilities
// Voice input, PWA install, camera, push notifications

// ── PWA Install Prompt ───────────────────────────────────────

let _deferredPrompt = null;

export function initPWA() {
  window.addEventListener("beforeinstallprompt", (e) => {
    e.preventDefault();
    _deferredPrompt = e;

    const btn = document.createElement("button");
    btn.textContent = "📲 Install App";
    btn.style.cssText =
      "font-size:12px;padding:7px 12px;border-radius:999px;border:1px solid rgba(125,211,252,.25);background:rgba(125,211,252,.12);color:rgba(255,255,255,.9);cursor:pointer;position:fixed;bottom:16px;right:16px;z-index:999;";
    btn.onclick = showInstallPrompt;
    btn.id = "pwa-install-btn";
    document.body.appendChild(btn);
  });

  window.addEventListener("appinstalled", () => {
    const btn = document.getElementById("pwa-install-btn");
    if (btn) btn.remove();
    _deferredPrompt = null;
  });
}

async function showInstallPrompt() {
  if (!_deferredPrompt) return;
  _deferredPrompt.prompt();
  const result = await _deferredPrompt.userChoice;
  if (result.outcome === "accepted") {
    const btn = document.getElementById("pwa-install-btn");
    if (btn) btn.remove();
  }
  _deferredPrompt = null;
}

// ── Voice Input (Web Speech API) ─────────────────────────────

export function initVoiceInput(inputElementId, onSubmit) {
  if (!("webkitSpeechRecognition" in window) && !("SpeechRecognition" in window)) {
    return null;
  }

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  const recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = true;
  recognition.lang = localStorage.getItem("visepanda_lang") || "en-US";

  const input = document.getElementById(inputElementId);
  if (!input) return null;

  // Create mic button
  const micBtn = document.createElement("button");
  micBtn.type = "button";
  micBtn.textContent = "🎤";
  micBtn.title = "Voice input";
  micBtn.style.cssText =
    "width:44px;height:44px;border-radius:999px;border:1px solid rgba(255,255,255,.08);background:rgba(255,255,255,.03);color:rgba(255,255,255,.7);cursor:pointer;font-size:18px;";
  micBtn.onclick = () => {
    if (micBtn.classList.contains("recording")) {
      recognition.stop();
    } else {
      recognition.start();
      micBtn.classList.add("recording");
      micBtn.textContent = "⏹";
      micBtn.style.borderColor = "rgba(252,165,165,.4)";
      micBtn.style.background = "rgba(252,165,165,.12)";
    }
  };

  recognition.onresult = (e) => {
    let transcript = "";
    for (let i = e.resultIndex; i < e.results.length; i++) {
      transcript += e.results[i][0].transcript;
    }
    input.value = transcript;
    if (e.results[0].isFinal) {
      recognition.stop();
      micBtn.classList.remove("recording");
      micBtn.textContent = "🎤";
      micBtn.style.borderColor = "rgba(255,255,255,.08)";
      micBtn.style.background = "rgba(255,255,255,.03)";
      if (onSubmit) setTimeout(onSubmit, 500);
    }
  };

  recognition.onerror = () => {
    micBtn.classList.remove("recording");
    micBtn.textContent = "🎤";
    micBtn.style.borderColor = "rgba(255,255,255,.08)";
    micBtn.style.background = "rgba(255,255,255,.03)";
  };

  recognition.onend = () => {
    micBtn.classList.remove("recording");
    micBtn.textContent = "🎤";
    micBtn.style.borderColor = "rgba(255,255,255,.08)";
    micBtn.style.background = "rgba(255,255,255,.03)";
  };

  // Insert mic button next to input
  input.parentNode.insertBefore(micBtn, input.nextSibling);

  return { stop: () => recognition.stop() };
}

// ── Camera / Landmark Recognition ────────────────────────────

export function initCameraButton(containerId) {
  if (!("mediaDevices" in navigator) || !("getUserMedia" in navigator.mediaDevices)) {
    return;
  }

  // Add a camera button to the chat footer area
  const footer = document.querySelector("footer");
  if (!footer) return;

  const camBtn = document.createElement("button");
  camBtn.type = "button";
  camBtn.textContent = "📷";
  camBtn.title = "Take a photo for landmark recognition";
  camBtn.style.cssText =
    "width:44px;height:44px;border-radius:999px;border:1px solid rgba(255,255,255,.08);background:rgba(255,255,255,.03);color:rgba(255,255,255,.7);cursor:pointer;font-size:18px;";

  camBtn.onclick = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: "environment" },
      });

      // Create camera overlay
      const overlay = document.createElement("div");
      overlay.style.cssText =
        "position:fixed;inset:0;background:rgba(0,0,0,.9);z-index:9999;display:flex;flex-direction:column;align-items:center;justify-content:center;";

      const video = document.createElement("video");
      video.srcObject = stream;
      video.autoplay = true;
      video.playsInline = true;
      video.style.cssText = "max-width:100%;max-height:70vh;border-radius:12px;";

      const captureBtn = document.createElement("button");
      captureBtn.textContent = "📸 Capture";
      captureBtn.style.cssText =
        "margin-top:16px;padding:12px 28px;border-radius:999px;border:none;background:rgba(125,211,252,.2);color:white;font-size:16px;cursor:pointer;";

      const closeBtn = document.createElement("button");
      closeBtn.textContent = "✕ Close";
      closeBtn.style.cssText =
        "position:absolute;top:20px;right:20px;padding:8px 16px;border-radius:999px;border:1px solid rgba(255,255,255,.2);background:none;color:white;cursor:pointer;font-size:14px;";

      overlay.appendChild(video);
      overlay.appendChild(captureBtn);
      overlay.appendChild(closeBtn);

      closeBtn.onclick = () => {
        stream.getTracks().forEach((t) => t.stop());
        overlay.remove();
      };

      captureBtn.onclick = () => {
        const canvas = document.createElement("canvas");
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        canvas.getContext("2d").drawImage(video, 0, 0);
        const dataUrl = canvas.toDataURL("image/jpeg", 0.8);
        stream.getTracks().forEach((t) => t.stop());
        overlay.remove();

        // Send to chat as "📷 [Landmark photo captured. What is this?]"
        const input = document.getElementById("msgInput");
        if (input) {
          input.value = `📷 I just took a photo — can you identify this landmark or place in China?`;
          input.form.dispatchEvent(new Event("submit", { cancelable: true }));
        }
      };

      document.body.appendChild(overlay);
    } catch (e) {
      alert("Camera access denied or unavailable.");
    }
  };

  footer.querySelector("form").appendChild(camBtn);
}

// ── Real-time Translation Toggle ─────────────────────────────

// ── Push Notification Setup ──────────────────────────────────

export async function requestPushPermission() {
  if (!("Notification" in window)) return false;
  if (Notification.permission === "granted") return true;
  if (Notification.permission === "denied") return false;
  const result = await Notification.requestPermission();
  return result === "granted";
}

export function notify(title, body, icon = "/assets/icon-192.png") {
  if (Notification.permission !== "granted") return;
  new Notification(title, { body, icon, badge: icon });
}
