#!/usr/bin/env node

const fs = require("node:fs");
const crypto = require("node:crypto");

const target = process.argv[2];
if (target == undefined || !target.endsWith(".json")) {
  console.error("Usage: ./src/test/http/run-in-terminal-with-filepath-to-get-signature.js src/test/resources/acceptancetests/webhook/user-workflow_run-requested.json");
  process.exit(1);
}

const payload = fs.readFileSync(target, "utf8");
const secret = "sample";

const signature = crypto.createHmac("sha256", secret).update(payload).digest("hex");

console.log("sha256=" + signature);
