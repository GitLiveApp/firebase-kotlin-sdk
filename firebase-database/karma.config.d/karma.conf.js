// Some tests are fluky in GitHub Actions, so we increase the timeout.
config.set({
    // The wasmJs suite can go quiet for longer than karma's default 30s while the SDK reconnects; above mocha's per-test
    // timeout, so a hanging test is reported by name instead of as a disconnected browser.
    browserNoActivityTimeout: 240000,
    client: {
      mocha: {
        timeout: 180000
      }
    },
});
