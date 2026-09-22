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

    [HttpGet]
    public async Task<ActionResult<List<CredentialDto>>> GetAll()
    {
        return Ok(await _service.GetAllAsync(Uid));
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<CredentialDto>> GetById(string id)
    {
        var result = await _service.GetByIdAsync(Uid, id);
        return result is null ? NotFound() : Ok(result);
    }

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

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(string id)
    {
        await _service.DeleteAsync(Uid, id);
        return NoContent();
    }
}
