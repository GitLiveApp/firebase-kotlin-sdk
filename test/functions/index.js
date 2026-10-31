const functions = require("firebase-functions");

const errors = {
  "invalid-argument": {
    message: "Invalid argument from emulator",
    httpResponseCode: 400,
  },
  "not-found": {
    message: "No data found from emulator",
    httpResponseCode: 404,
  },
  "permission-denied": {
    message: "Permission denied from emulator",
    httpResponseCode: 403,
  },
};

exports.throwHttpsError = functions.https.onCall((dataOrRequest) => {
  const data = dataOrRequest && typeof dataOrRequest.data === "object"
    ? dataOrRequest.data
    : dataOrRequest;
  const code = data && data.code;
  const error = errors[code] || {
    message: "Unknown error from emulator",
    httpResponseCode: 500,
  };

  throw new functions.https.HttpsError(code || "internal", error.message, {
    reason: code,
    httpResponseCode: error.httpResponseCode,
  });
});

exports.echo = functions.https.onCall((dataOrRequest) => {
  // v2 passes a request whose data is the call data; v1 passes the data itself
  return dataOrRequest && typeof dataOrRequest === "object" && "rawRequest" in dataOrRequest ? dataOrRequest.data : dataOrRequest;
});
