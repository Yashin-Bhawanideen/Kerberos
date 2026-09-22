using Kerberos.Api.Models;
using Kerberos.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Kerberos.Api.Controllers;

[ApiController]
[Route("api/credentials")]
[Authorize] // requires a valid Firebase ID token on every request
public class CredentialsController : ControllerBase
{
    private readonly CredentialService _service;

    public CredentialsController(CredentialService service) => _service = service;

    // Firebase ID tokens carry the user's uid in the "user_id" claim
    private string Uid => User.FindFirst("user_id")?.Value
        ?? throw new UnauthorizedAccessException("No uid in token");

    // GET /api/credentials
    // Returns every credential belonging to the authenticated user
    [HttpGet]
    public async Task<ActionResult<List<CredentialDto>>> GetAll()
    {
        return Ok(await _service.GetAllAsync(Uid));
    }

    // GET /api/credentials/{id}
    // Returns a single credential by id, scoped to the authenticated user
    [HttpGet("{id}")]
    public async Task<ActionResult<CredentialDto>> GetById(string id)
    {
        var result = await _service.GetByIdAsync(Uid, id);
        return result is null ? NotFound() : Ok(result);
    }

    // POST /api/credentials
    // Creates a new credential entry for the authenticated user
    [HttpPost]
    public async Task<ActionResult<CredentialDto>> Create(CreateCredentialRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.ServiceName) ||
            string.IsNullOrWhiteSpace(request.Username) ||
            string.IsNullOrWhiteSpace(request.Password))
        {
            return BadRequest("serviceName, username, and password are required.");
        }

        var created = await _service.CreateAsync(Uid, request);
        return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
    }
    // DELETE /api/credentials/{id}
    // Deletes a credential by id, scoped to the authenticated user
    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(string id)
    {
        await _service.DeleteAsync(Uid, id);
        return NoContent();
    }
}
/*
 References
Gideon, 2012. Difference between ApiController and Controller in ASP.NET MVC. [Online] 
Available at: https://stackoverflow.com/questions/9494966/difference-between-apicontroller-and-controller-in-asp-net-mvc
Microsoft, 2024. Tutorial: Create a controller-based web API with ASP.NET Core. [Online] 
Available at: https://learn.microsoft.com/en-us/aspnet/core/tutorials/first-web-api?view=aspnetcore-10.0&tabs=visual-studio
Microsoft, 2026. Create web APIs with ASP.NET Core. [Online] 
Available at: https://learn.microsoft.com/en-us/aspnet/core/web-api/?view=aspnetcore-10.0


 */