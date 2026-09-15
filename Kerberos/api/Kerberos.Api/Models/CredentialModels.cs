namespace Kerberos.Api.Models;

public class CredentialDto
{
    public string Id { get; set; } = "";
    public string ServiceName { get; set; } = "";
    public string Username { get; set; } = "";
    public string Password { get; set; } = "";
    public string WebsiteUrl { get; set; } = "";
    public string Notes { get; set; } = "";
    public long CreatedAt { get; set; }
    public long ModifiedAt { get; set; }
}

public class CreateCredentialRequest
{
    public string ServiceName { get; set; } = "";
    public string Username { get; set; } = "";
    public string Password { get; set; } = "";
    public string WebsiteUrl { get; set; } = "";
    public string Notes { get; set; } = "";
}
