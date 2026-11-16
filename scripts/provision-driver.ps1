param(
    [Parameter(Mandatory = $true)]
    [string] $DisplayName,

    [Parameter(Mandatory = $true)]
    [string] $Phone,

    [string] $SupabaseUrl = $env:SUPABASE_URL
)

$ErrorActionPreference = "Stop"

if ($Phone -notmatch '^\+628[0-9]{8,12}$') {
    throw "Phone must use Indonesian E.164 format, for example +628123456789."
}

if ([string]::IsNullOrWhiteSpace($DisplayName)) {
    throw "DisplayName cannot be empty."
}

if ([string]::IsNullOrWhiteSpace($SupabaseUrl)) {
    throw "Set SUPABASE_URL or pass -SupabaseUrl."
}

$serviceRoleKey = $env:SUPABASE_SERVICE_ROLE_KEY
if ([string]::IsNullOrWhiteSpace($serviceRoleKey)) {
    throw "Set SUPABASE_SERVICE_ROLE_KEY for this terminal session."
}

$securePassword = Read-Host "Temporary password" -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)

try {
    $temporaryPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    if ($temporaryPassword.Length -lt 8 -or
        $temporaryPassword -notmatch '[A-Za-z]' -or
        $temporaryPassword -notmatch '[0-9]') {
        throw "Temporary password must contain at least 8 characters, a letter, and a digit."
    }

    $emailAlias = "driver.$($Phone.TrimStart('+'))@shuttlespot.internal"

    # Only the local admin command may attach the provisioning marker.
    $body = @{
        email = $emailAlias
        password = $temporaryPassword
        email_confirm = $true
        app_metadata = @{
            provisioned_role = "driver"
            display_name = $DisplayName.Trim()
            phone_e164 = $Phone
        }
    } | ConvertTo-Json -Depth 4

    $headers = @{
        apikey = $serviceRoleKey
        Authorization = "Bearer $serviceRoleKey"
    }

    $response = Invoke-RestMethod `
        -Method Post `
        -Uri "$($SupabaseUrl.TrimEnd('/'))/auth/v1/admin/users" `
        -Headers $headers `
        -ContentType "application/json" `
        -Body $body

    Write-Host "Driver provisioned: $($response.id) ($Phone)"
}
finally {
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    $temporaryPassword = $null
}
