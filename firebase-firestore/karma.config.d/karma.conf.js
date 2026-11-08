// Some tests are fluky in GitHub Actions, so we increase the timeout.
config.set({
    // The wasmJs suite can go quiet for longer than karma's default 30s while the SDK reconnects.
    browserNoActivityTimeout: 180000,
    client: {
      mocha: {
        timeout: 1800000
      }
    },
});
