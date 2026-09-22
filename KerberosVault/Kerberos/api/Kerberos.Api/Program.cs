using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore;
using Kerberos.Api.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

var firebaseProjectId = builder.Configuration["Firebase:ProjectId"]
    ?? throw new Exception("Firebase:ProjectId not configured");

// Initialize Firebase Admin SDK (used for Firestore access from the server).
// You supply firebase-service-account.json yourself (Firebase console ->
// Project Settings -> Service Accounts -> Generate new private key).
FirebaseApp.Create(new AppOptions
{
    Credential = GoogleCredential.FromFile("firebase-service-account.json")
});

// Firestore client — explicitly pointed at the same service account file,
// since it does NOT share credentials with FirebaseApp above
var firestoreDb = new FirestoreDbBuilder
{
    ProjectId = firebaseProjectId,
    CredentialsPath = "firebase-service-account.json"
}.Build();

builder.Services.AddSingleton(firestoreDb);

// Validate Firebase ID tokens on every incoming request
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.Authority = $"https://securetoken.google.com/{firebaseProjectId}";
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = $"https://securetoken.google.com/{firebaseProjectId}",
            ValidateAudience = true,
            ValidAudience = firebaseProjectId,
            ValidateLifetime = true
        };
    });

builder.Services.AddAuthorization();
builder.Services.AddScoped<CredentialService>();
builder.Services.AddControllers();

var app = builder.Build();

app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
