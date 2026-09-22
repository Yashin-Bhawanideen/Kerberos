using System.Security.Cryptography;
using System.Text;
using Google.Cloud.Firestore;
using Kerberos.Api.Models;

namespace Kerberos.Api.Services;

public class CredentialService
{
    private readonly FirestoreDb _db;
    private readonly byte[] _key;

    public CredentialService(FirestoreDb db, IConfiguration config)
    {
        _db = db;

        var keyString = config["Encryption:Key"]
            ?? throw new InvalidOperationException("Encryption:Key is not configured.");
        _key = Convert.FromBase64String(keyString);
    }

    // Each user's credentials live under their own document in "users",
    // in a "credentials" subcollection — so all queries are naturally scoped per-uid
    private CollectionReference CredentialsCollection(string uid) =>
        _db.Collection("users").Document(uid).Collection("credentials");

    public async Task<List<CredentialDto>> GetAllAsync(string uid)
    {
        var snapshot = await CredentialsCollection(uid).GetSnapshotAsync();
        return snapshot.Documents
            .Select(d => MapToDto(d.Id, d))
            .OrderByDescending(c => c.ModifiedAt)
            .ToList();
    }

    // Fetches a single credential by id; returns null if it doesn't exist
    public async Task<CredentialDto?> GetByIdAsync(string uid, string id)
    {
        var doc = await CredentialsCollection(uid).Document(id).GetSnapshotAsync();
        return doc.Exists ? MapToDto(doc.Id, doc) : null;
    }

    public async Task<CredentialDto> CreateAsync(string uid, CreateCredentialRequest request)
{
    var now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    //uses the same id as the local Room credential
    var docRef = CredentialsCollection(uid).Document(request.Id);

    var data = new Dictionary<string, object>
    {
        { "serviceName", request.ServiceName },
        { "username", request.Username },
        { "password", Encrypt(request.Password) },
        { "websiteUrl", request.WebsiteUrl },
        { "notes", request.Notes },
        { "createdAt", now },
        { "modifiedAt", now }
    };

    await docRef.SetAsync(data);

    return new CredentialDto
    {
        Id = request.Id,
        ServiceName = request.ServiceName,
        Username = request.Username,
        Password = request.Password,
        WebsiteUrl = request.WebsiteUrl,
        Notes = request.Notes,
        CreatedAt = now,
        ModifiedAt = now
    };
}

//updates the existing Firestore document (Google Cloud, 2026)
public async Task<CredentialDto?> UpdateAsync(
    string uid,
    string id,
    UpdateCredentialRequest request)
{
    var docRef = CredentialsCollection(uid).Document(id);
    var existing = await docRef.GetSnapshotAsync();

    if (!existing.Exists)
    {
        return null;
    }

    var now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    var data = new Dictionary<string, object>
    {
        { "serviceName", request.ServiceName },
        { "username", request.Username },
        { "password", Encrypt(request.Password) },
        { "websiteUrl", request.WebsiteUrl },
        { "notes", request.Notes },
        { "modifiedAt", now }
    };

    await docRef.UpdateAsync(data);

    return new CredentialDto
    {
        Id = id,
        ServiceName = request.ServiceName,
        Username = request.Username,
        Password = request.Password,
        WebsiteUrl = request.WebsiteUrl,
        Notes = request.Notes,
        CreatedAt = existing.GetValue<long>("createdAt"),
        ModifiedAt = now
    };
}

    public async Task DeleteAsync(string uid, string id)
    {
        await CredentialsCollection(uid).Document(id).DeleteAsync();
    }

    // Converts a Firestore document into a CredentialDto
    // websiteUrl/notes are optional fields, so they're defaulted to "" if missing
    private CredentialDto MapToDto(string id, DocumentSnapshot doc) => new()
    {
        Id = id,
        ServiceName = doc.GetValue<string>("serviceName"),
        Username = doc.GetValue<string>("username"),
        Password = Decrypt(doc.GetValue<string>("password")),
        WebsiteUrl = doc.ContainsField("websiteUrl") ? doc.GetValue<string>("websiteUrl") : "",
        Notes = doc.ContainsField("notes") ? doc.GetValue<string>("notes") : "",
        CreatedAt = doc.GetValue<long>("createdAt"),
        ModifiedAt = doc.GetValue<long>("modifiedAt")
    };

    private string Encrypt(string plainText)
    {
        using var aes = Aes.Create();
        aes.Key = _key;
        aes.GenerateIV();

        using var encryptor = aes.CreateEncryptor();
        var plainBytes = Encoding.UTF8.GetBytes(plainText);
        var cipherBytes = encryptor.TransformFinalBlock(plainBytes, 0, plainBytes.Length);

        // Prepend the IV so Decrypt can read it back out later
        var result = new byte[aes.IV.Length + cipherBytes.Length];
        Buffer.BlockCopy(aes.IV, 0, result, 0, aes.IV.Length);
        Buffer.BlockCopy(cipherBytes, 0, result, aes.IV.Length, cipherBytes.Length);

        return Convert.ToBase64String(result);
    }

    private string Decrypt(string cipherText)
    {
        var fullBytes = Convert.FromBase64String(cipherText);

        using var aes = Aes.Create();
        aes.Key = _key;

        var iv = new byte[16];
        Buffer.BlockCopy(fullBytes, 0, iv, 0, iv.Length);
        aes.IV = iv;

        var cipherBytes = new byte[fullBytes.Length - iv.Length];
        Buffer.BlockCopy(fullBytes, iv.Length, cipherBytes, 0, cipherBytes.Length);

        using var decryptor = aes.CreateDecryptor();
        var plainBytes = decryptor.TransformFinalBlock(cipherBytes, 0, cipherBytes.Length);

        return Encoding.UTF8.GetString(plainBytes);
    }
}

/*
REFERENCE LIST

Google Cloud. 2026. Add data to Cloud Firestore. [Online].
Available at: https://cloud.google.com/firestore/docs/manage-data/add-data
[Accessed 22 September 2026].
*/