namespace Kerberos.Api.Models;

// Data returned to the client for a stored credential
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

// Payload used when creating a new credential
public class CreateCredentialRequest
{
    public string ServiceName { get; set; } = "";
    public string Username { get; set; } = "";
    public string Password { get; set; } = "";
    public string WebsiteUrl { get; set; } = "";
    public string Notes { get; set; } = "";
}
/*
 References
Arianme, n.d. Models in ASP.NET Core Web API. [Online] 
Available at: https://stackoverflow.com/questions/66576344/models-in-asp-net-core-web-api


 */