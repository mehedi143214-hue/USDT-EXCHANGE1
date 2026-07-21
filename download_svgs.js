const fs = require('fs');

async function downloadSvg(url, filename) {
    const res = await fetch(url, { headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" }});
    const text = await res.text();
    fs.writeFileSync(filename, text);
    console.log(`Saved ${filename}`);
}

async function main() {
    await downloadSvg("https://upload.wikimedia.org/wikipedia/commons/1/1b/PhonePe_Logo.svg", "phonepe_color.svg");
    await downloadSvg("https://upload.wikimedia.org/wikipedia/commons/f/f2/Google_Pay_Logo.svg", "gpay_color.svg");
    await downloadSvg("https://upload.wikimedia.org/wikipedia/commons/c/cb/Paytm_Logo_stand_for_Pay_through_mobile.svg", "paytm_color.svg");
    await downloadSvg("https://upload.wikimedia.org/wikipedia/commons/f/fc/UPI-Logo.svg", "upi_color.svg");
}

main();
