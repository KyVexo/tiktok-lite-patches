"use strict";

const english = {
  features: "Features", install: "Installation", language: "Language",
  title: "TikTok Lite, <em>your way.</em>",
  intro: "Filter feed ads, save videos without a watermark when a clean source is available, and control both patches in Mod settings.",
  add: "Add to Morphe", releases: "View releases",
  meta: "Preview v0.1.0-dev.21 · Tested on Galaxy A36",
  panelAlt: "Mod settings with Feed and Downloads cards",
  feed: "Feed", feedCopy: "Filters detected ads in two feed paths and shows how many were filtered in this session.",
  downloads: "Downloads", downloadsCopy: "Prefers the watermark-free version for permitted downloads when available.",
  settings: "Mod settings", settingsCopy: "Manage patches from Settings and privacy. The panel follows that screen’s theme and supports eight languages.",
  gallery: "Screenshots", nativeAlt: "Mod settings entry inside Settings and privacy",
  nativeCaption: "Entry in Settings and privacy", aboutAlt: "About dialog with avatar and patch version",
  aboutCaption: "About TikTok Lite Patches",
  installIntro: "The tested workflow uses Morphe Desktop with the ARM64 splits for TikTok Lite 47.8.15. Full installation through Morphe Manager is still being tested.",
  step1: "Download the .mpp bundle from GitHub Releases and obtain the original APKs from your installation.",
  step2: "Apply Hide feed ads to base.apk. In a separate run, apply the other two patches to split_df_edit_filter.apk.",
  step3: "Keep the split filenames and use the same package ID and certificate for the whole set.",
  guide: "Read the GitHub guide", stats: "Bundle downloads", loading: "Checking GitHub…",
  footer: "Independent of TikTok and Morphe · Jair1c"
};

const language = document.querySelector("#language");
const stats = document.querySelector("#stats-list");
const message = document.querySelector("#stats-message");
const localized = [...document.querySelectorAll("[data-i], [data-alt]")].map(node => ({
  node,
  spanish: node.dataset.alt ? node.alt : node.innerHTML
}));
let releases;
let failed = false;

function render() {
  const en = language.value === "en";
  document.documentElement.lang = language.value;
  for (const {node, spanish} of localized) {
    const key = node.dataset.alt || node.dataset.i;
    if (node.dataset.alt) node.alt = en ? english[key] : spanish;
    else node.innerHTML = en ? english[key] : spanish;
  }
  stats.replaceChildren();
  if (failed) {
    message.textContent = en ? "GitHub statistics are unavailable." : "Las estadísticas de GitHub no están disponibles.";
    return;
  }
  if (!releases) return;
  message.textContent = en ? "Downloads of patch bundles per release." : "Descargas de archivos de parches por versión.";
  const formatter = new Intl.NumberFormat(language.value);
  for (const release of releases) {
    const item = document.createElement("li");
    const link = document.createElement("a");
    const count = document.createElement("span");
    link.href = release.html_url;
    link.textContent = release.tag_name;
    count.textContent = formatter.format(release.assets.filter(asset => asset.name.endsWith(".mpp"))
      .reduce((total, asset) => total + asset.download_count, 0));
    item.append(link, count);
    stats.append(item);
  }
}

try { language.value = localStorage.getItem("lite-patches-language") === "en" ? "en" : "es"; } catch {}
language.addEventListener("change", () => {
  try { localStorage.setItem("lite-patches-language", language.value); } catch {}
  render();
});
render();

fetch("https://api.github.com/repos/jair1c/tiktok-lite-patches/releases?per_page=50")
  .then(response => {
    if (!response.ok) throw new Error("GitHub releases unavailable");
    return response.json();
  })
  .then(data => { releases = data.filter(release => !release.draft); })
  .catch(() => { failed = true; })
  .finally(render);
