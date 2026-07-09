import { generate } from "npm:youtube-po-token-generator";
import { dirname, join, fromFileUrl } from "jsr:@std/path";

async function run() {
  try {

    const scriptDir = dirname(fromFileUrl(import.meta.url));
    const targetPath = join(scriptDir, "po_token.html");

    console.log("Fetching YouTube PO Token...");
    const data = await generate();

    const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>YouTube PO Token</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; background-color: #f4f4f7; color: #333; padding: 40px 20px; display: flex; justify-content: center; }
        .container { background: #ffffff; padding: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); max-width: 650px; width: 100%; box-sizing: border-box; }
        h1 { color: #ff0000; font-size: 24px; margin-top: 0; border-bottom: 2px solid #f4f4f7; padding-bottom: 10px; }
        .label { font-weight: bold; color: #555; margin-top: 20px; margin-bottom: 5px; font-size: 14px; text-transform: uppercase; }
        .token-box { background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; padding: 12px; font-family: monospace; font-size: 14px; word-break: break-all; white-space: pre-wrap; margin: 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1>YouTube PO Token Data</h1>
        <div class="label">Visitor Data (visitorData)</div>
        <pre class="token-box">${data.visitorData}</pre>
        <div class="label">PO Token (potokennp2)</div>
        <pre class="token-box">${data.poToken}</pre>
    </div>
</body>
</html>`;

    await Deno.writeTextFile(targetPath, htmlContent);
    console.log(`Successfully generated token file at: ${targetPath}`);

  } catch (error) {
    console.error("Failed to generate or save token:", error);
  }
}

await run();
