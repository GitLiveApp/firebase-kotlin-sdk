// Some tests are fluky in GitHub Actions, so we increase the timeout.
config.set({
    client: {
      mocha: {
        timeout: 180000
      }
    },
});
