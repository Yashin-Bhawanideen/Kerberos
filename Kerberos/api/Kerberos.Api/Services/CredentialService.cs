using Google.Cloud.Firestore;
using Kerberos.Api.Models;

namespace Kerberos.Api.Services;

public class CredentialService
{
    private readonly FirestoreDb _db;

    public CredentialService(FirestoreDb db) => _db = db;

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

    public async Task<CredentialDto?> GetByIdAsync(string uid, string id)
    {
        var doc = await CredentialsCollection(uid).Document(id).GetSnapshotAsync();
        return doc.Exists ? MapToDto(doc.Id, doc) : null;
    }

    public async Task<CredentialDto> CreateAsync(string uid, CreateCredentialRequest request)
    {
        var now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var docRef = CredentialsCollection(uid).Document();

        var data = new Dictionary<string, object>
        {
            { "serviceName", request.ServiceName },
            { "username", request.Username },
            { "password", request.Password },
            { "websiteUrl", request.WebsiteUrl },
            { "notes", request.Notes },
            { "createdAt", now },
            { "modifiedAt", now }
        };

        await docRef.SetAsync(data);

        return new CredentialDto
        {
            Id = docRef.Id,
            ServiceName = request.ServiceName,
            Username = request.Username,
            Password = request.Password,
            WebsiteUrl = request.WebsiteUrl,
            Notes = request.Notes,
            CreatedAt = now,
            ModifiedAt = now
        };
    }

    public async Task DeleteAsync(string uid, string id)
    {
        await CredentialsCollection(uid).Document(id).DeleteAsync();
    }

    private static CredentialDto MapToDto(string id, DocumentSnapshot doc) => new()
    {
        Id = id,
        ServiceName = doc.GetValue<string>("serviceName"),
        Username = doc.GetValue<string>("username"),
        Password = doc.GetValue<string>("password"),
        WebsiteUrl = doc.ContainsField("websiteUrl") ? doc.GetValue<string>("websiteUrl") : "",
        Notes = doc.ContainsField("notes") ? doc.GetValue<string>("notes") : "",
        CreatedAt = doc.GetValue<long>("createdAt"),
        ModifiedAt = doc.GetValue<long>("modifiedAt")
    };
}
