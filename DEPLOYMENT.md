# Deployment Guide

This guide covers both **day-to-day releases** (if you already have everything set up) and **first-time setup** for deploying the Angus Software App to Google Play Store (Android) and GitHub Pages (Web/Wasm).

---

## Quick Release (Day-to-Day)

> **Already have GitHub Actions, Pages, and Google Play configured?** Follow these steps to push a new release.

### 1. Bump the version

Choose the appropriate bump level and run the Gradle task:

```powershell
# Patch release (x.y.z → x.y.(z+1)) — most common for bug fixes/small updates
./gradlew releasePatch

# Minor release (x.y.z → x.(y+1).0) — new features, backward compatible
./gradlew releaseMinor

# Major release ((x+1).0.0) — breaking changes
./gradlew releaseMajor
```

Each task updates **both** `version` (human-readable) and `android.versionCode` (+1) in `gradle.properties`.

### 2. Commit the version bump

```powershell
git add gradle.properties
$ver = (Select-String '^version=' .\gradle.properties).ToString().Split('=')[1].Trim()
git commit -m "chore: release $ver"
```

### 3. Push a release branch to trigger CI

```powershell
git checkout -b release/v$ver
git push origin HEAD
```

### 4. Monitor and verify

- **GitHub Actions**: Watch the workflow at <https://github.com/RhoMancer/AngusSoftwareApp/actions>
- **Google Play Console**: Confirm the new build appears in **Internal Testing** with the correct `versionName` and `versionCode`
- **GitHub Pages**: Visit <https://rhomancer.github.io/AngusSoftwareApp/> and check the version badge

### 5. Merge back to main

After CI passes, open a PR from `release/v<version>` → `main` and merge to keep `gradle.properties` updated on main.

### 6. (Optional) Tag the release

```powershell
git tag v$ver
git push origin v$ver
```

---

## First-Time Setup

> **New to this project or setting up deployment for the first time?** Follow the sections below.

## Overview

The deployment pipeline automatically triggers when you push to `release/**` branches and does the following:
1. Runs all unit tests
2. Builds and deploys Android app to Google Play Internal Testing track
3. Builds and deploys Web/Wasm app to GitHub Pages

## Prerequisites

- GitHub repository: <https://github.com/RhoMancer/AngusSoftwareApp
- Google Play Developer account
- Access to repository settings (for adding secrets)

---

## Step 1: Create Android Keystore

A keystore is required to sign your Android release builds.

### 1.1 Generate a new keystore

Open a terminal in your project root and run:

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias angus_software_key
```

You'll be prompted for:
- **Keystore password**: Choose a strong password (you'll need this later)
- **Key password**: Choose a strong password (can be same as keystore password)
- **Name, Organization, etc.**: Fill in your information

**IMPORTANT**: 
- Save the keystore file (`release-keystore.jks`) in a secure location
- **Never commit this file to version control** (already in .gitignore)
- Keep a backup of this file and passwords - losing it means you can't update your app!

### 1.2 Create local keystore.properties file

For local builds, copy the template:

```bash
cp keystore.properties keystore.properties
```

Edit `keystore.properties` and fill in:
```properties
storeFile=/absolute/path/to/your/release-keystore.jks
storePassword=your_keystore_password
keyAlias=angus_software_key
keyPassword=your_key_password
```

---

## Step 2: Set Up Google Play Console

### 2.1 Create app listing

1. Go to [Google Play Console](https://play.google.com/console)
2. Click **Create app**
3. Fill in app details:
   - **App name**: Angus Software App (or your preferred name)
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free
4. Complete the required sections (Store listing, Content rating, etc.)

### 2.2 Create a service account for API access

1. In Google Play Console, go to **Setup** → **API access**
2. Click **Create new service account**
3. Follow the link to Google Cloud Console
4. Create a new service account:
   - Name: `github-actions-deploy`
   - Role: Select **Service Accounts** → **Service Account User**
5. Click **Done**, then **Create and continue**
6. Grant the service account access:
   - Go back to Google Play Console → **API access**
   - Find your service account and click **Grant access**
   - Under **App permissions**, select your app
   - Under **Account permissions**, select:
     - **Releases**: Create and edit releases (including Internal Testing track)
   - Click **Invite user** then **Send invite**

### 2.3 Download service account JSON key

1. In Google Cloud Console, go to **IAM & Admin** → **Service Accounts**
2. Find your `github-actions-deploy` service account
3. Click on it, then go to **Keys** tab
4. Click **Add Key** → **Create new key**
5. Choose **JSON** format
6. Save the downloaded JSON file securely (you'll need it in Step 3)

---

## Step 3: Configure GitHub Secrets

GitHub secrets store sensitive information securely for use in GitHub Actions.

### 3.1 Navigate to repository secrets

1. Go to <https://github.com/RhoMancer/AngusSoftwareApp>
2. Click **Settings** tab
3. In left sidebar, go to **Secrets and variables** → **Actions**
4. Click **New repository secret**

### 3.2 Add the following secrets

#### KEYSTORE_BASE64
Your keystore file encoded in base64.

**On Windows (PowerShell):**
```powershell
$bytes = [System.IO.File]::ReadAllBytes("C:\path\to\your\release-keystore.jks")
$base64 = [System.Convert]::ToBase64String($bytes)
$base64 | Set-Clipboard
```

**On macOS/Linux:**
```bash
base64 -i release-keystore.jks | pbcopy
# or without clipboard:
base64 -i release-keystore.jks
```

Paste the output as the secret value.

#### KEYSTORE_PASSWORD
The password you used when creating the keystore.

#### KEY_ALIAS
The alias you used when creating the keystore (e.g., `angus_software_key`).

#### KEY_PASSWORD
The key password (might be the same as KEYSTORE_PASSWORD).

#### GOOGLE_PLAY_SERVICE_ACCOUNT_JSON
The entire contents of the JSON file you downloaded in Step 2.3.

Open the JSON file in a text editor, copy all contents, and paste as the secret value.

### 3.3 Verify secrets

After adding all secrets, you should have:
- ✅ KEYSTORE_BASE64
- ✅ KEYSTORE_PASSWORD
- ✅ KEY_ALIAS
- ✅ KEY_PASSWORD
- ✅ GOOGLE_PLAY_SERVICE_ACCOUNT_JSON

---

## Step 4: Enable GitHub Pages

### 4.1 Configure Pages deployment

1. In your repository, go to **Settings** → **Pages**
2. Under **Source**, select **Deploy from a branch**
3. Under **Branch**, select `gh-pages` (it will be created automatically on first deployment)
4. Click **Save**

### 4.2 Grant workflow permissions

1. Go to **Settings** → **Actions** → **General**
2. Scroll to **Workflow permissions**
3. Select **Read and write permissions**
4. Check **Allow GitHub Actions to create and approve pull requests**
5. Click **Save**

---

## Step 5: First Deployment

### 5.1 Update version information (automated)

This project uses file-based versioning. The single source of truth is in `gradle.properties`:

```
version=1.2.1           # human-readable SemVer → Android versionName, shown on Web/Wasm
android.versionCode=12  # Play Store versionCode → MUST increment by exactly +1 per release
```

You do NOT edit `composeApp/build.gradle.kts` for versions anymore. Android reads these values automatically.

Use the Gradle tasks below to bump versions:

```
# Patch release (x.y.z → x.y.(z+1)) and versionCode +1
gradlew releasePatch

# Minor release (x.y.z → x.(y+1).0) and versionCode +1
gradlew releaseMinor

# Major release ((x+1).0.0) and versionCode +1
gradlew releaseMajor
```

After running one of the above, commit the updated `gradle.properties` and proceed.

### 5.2 Create a release branch

```bash
git checkout -b release/v1.0
git push origin release/v1.0
```

### 5.3 Monitor the deployment

1. Go to your repository on GitHub
2. Click **Actions** tab
3. You should see a workflow run for "Release Deployment"
4. Click on it to see progress and logs

The workflow will:
- ✅ Run unit tests
- ✅ Build Android release (APK + AAB)
- ✅ Upload to Google Play Internal Testing
- ✅ Build Web/Wasm distribution
- ✅ Deploy to GitHub Pages

Versions during deployment
- The GitHub Actions workflow can optionally perform the bump for you prior to build and commit it back to the branch. Example snippet:

```yaml
- name: Bump version for release
  run: ./gradlew releasePatch

- name: Commit version bump
  run: |
    git config user.name "github-actions"
    git config user.email "github-actions@users.noreply.github.com"
    git add gradle.properties
    git commit -m "chore: release $(grep ^version= gradle.properties | cut -d= -f2) (code $(grep ^android.versionCode= gradle.properties | cut -d= -f2))" || echo "No changes"
    git push
```

Note: If your repository policy disallows CI commits, run the bump task locally and push the change in a PR before triggering the release workflow.

### 5.4 Verify deployments

**Android (Google Play):**
1. Go to Google Play Console → Your app → **Internal testing**
2. You should see your release listed
3. Share the internal testing link with testers or yourself
4. Install and verify the app works

**Web (GitHub Pages):**
1. Go to <https://rhomancer.github.io/AngusSoftwareApp/>
2. Your web app should be live
3. Test the functionality

---

## Step 6: Publishing to Production

Once you've tested your app on Internal Testing and are ready for public release:

### 6.1 Promote in Google Play Console

1. Go to **Internal testing** → Select your release
2. Click **Promote release** → Choose your target track:
   - **Closed testing**: For a larger group of testers
   - **Open testing**: Public beta
   - **Production**: Public release

### 6.2 (Optional) Update workflow for production

If you want automatic production deployment instead of internal testing, edit `.github/workflows/release-deploy.yml`:

```yaml
- name: Deploy to Google Play Production
  uses: r0adkll/upload-google-play@v1
  with:
    serviceAccountJsonPlainText: ${{ secrets.GOOGLE_PLAY_SERVICE_ACCOUNT_JSON }}
    packageName: dev.angussoftware.app
    releaseFiles: composeApp/build/outputs/bundle/release/*.aab
    track: production  # Changed from 'internal' to 'production'
    status: completed
```

---

## Troubleshooting

### Build fails with "keystore not found"
- Check that all GitHub secrets are set correctly
- Verify KEYSTORE_BASE64 is the complete base64-encoded keystore

### Google Play upload fails
- Ensure service account has correct permissions in Play Console
- Verify GOOGLE_PLAY_SERVICE_ACCOUNT_JSON is the complete JSON (including curly braces)
- Check that the app listing exists in Play Console
- Ensure versionCode is higher than any previous upload

### GitHub Pages not updating
- Check that workflow permissions are set to "Read and write"
- Verify the gh-pages branch was created
- Go to Settings → Pages and ensure source is set to "gh-pages" branch

### Tests failing
- Run tests locally first: `./gradlew :composeApp:testDebugUnitTest`
- Check the test logs in GitHub Actions for specific failures
- Fix failing tests before pushing to release branch

---

## Future Enhancements

### Add custom domain to GitHub Pages

1. Purchase a domain from a registrar
2. Add DNS records pointing to GitHub Pages
3. In repository Settings → Pages, add your custom domain
4. Update `.github/workflows/release-deploy.yml`:
   ```yaml
   - name: Deploy to GitHub Pages
     uses: peaceiris/actions-gh-pages@v4
     with:
       github_token: ${{ secrets.GITHUB_TOKEN }}
       publish_dir: composeApp/build/dist/wasmJs/productionExecutable
       publish_branch: gh-pages
       cname: yourdomain.com  # Add this line
   ```

### Add release notes automation

Consider using release notes from Git commits or a CHANGELOG file for your Play Store releases.

---

## Security Best Practices

- ✅ Never commit keystore files or passwords to version control
- ✅ Regularly rotate service account keys
- ✅ Use strong, unique passwords for keystores
- ✅ Keep backup copies of keystores in secure, offline storage
- ✅ Limit service account permissions to only what's needed
- ✅ Review GitHub Actions logs but don't expose secrets in logs

---

## Additional Resources

- [Google Play Console Help](https://support.google.com/googleplay/android-developer)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Pages Documentation](https://docs.github.com/en/pages)
- [Android App Signing Best Practices](https://developer.android.com/studio/publish/app-signing)

---

## Support

If you encounter issues not covered in this guide, please:
1. Check GitHub Actions logs for specific error messages
2. Review the troubleshooting section above
3. Open an issue in the repository with detailed error information
