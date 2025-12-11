# TimeFlow - New Features Guide

## Calendar Integration

TimeFlow now supports syncing your class schedules with external calendar applications!

### Features Available:

#### 1. Export to iCalendar (.ics)
- **What it does**: Creates a calendar file that works with any calendar app (Google Calendar, Apple Calendar, Outlook, etc.)
- **How to use**:
  1. Go to your timetable view
  2. Click the "Export to Calendar (.ics)" button at the bottom
  3. Choose where to save the file
  4. Import the file into your preferred calendar application

#### 2. Sync to Google Calendar
- **What it does**: Directly syncs your timetable to your Google Calendar account
- **Prerequisites**: 
  - You need a `credentials.json` file in the project root directory
  - Get this from Google Cloud Console by creating a project and enabling the Calendar API
- **How to use**:
  1. Set up Google Calendar API credentials (see setup guide below)
  2. Go to your timetable view
  3. Click "Sync to Google Calendar"
  4. Authorize the application when prompted
  5. Your classes will be added to your Google Calendar automatically

#### 3. Email Timetable
- **What it does**: Sends your complete timetable to your registered email
- **How to use**:
  1. Go to your timetable view
  2. Click "Email My Timetable"
  3. Check your email inbox

### Setting Up Google Calendar Integration

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google Calendar API
4. Create OAuth 2.0 credentials
5. Download the credentials JSON file
6. Rename it to `credentials.json` and place it in the TimeFlow project root directory

## Automated Email Reminders

TimeFlow now automatically sends email reminders for your classes!

### How It Works:

- **Daily Reminders**: Every morning at 8:00 AM, you'll receive an email with your schedule for the day
- **Automatic**: No setup required - it runs automatically when the application starts
- **Smart**: Only sends reminders on weekdays and only for approved classes
- **Personalized**: Different reminders for students and lecturers

### What's Included in Reminders:

For Students:
- List of all classes for the day
- Time, room, course name, and lecturer information
- Organized chronologically

For Lecturers:
- Classes they're teaching that day
- Time and room information

### Configuring Email Settings:

Make sure your `.env` file has the correct email configuration:

```bash
SMTP_USERNAME=your-email@domain.com
SMTP_PASSWORD=your-app-password
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SEND_FROM=noreply@timeflow.com
```

**Note**: For Gmail, you'll need to use an App Password, not your regular password.

## UI Improvements

The user interface has been completely refreshed with:

### Visual Enhancements:
- Modern color scheme with blue, green, yellow, and red accents
- Better button styling with hover effects
- Improved spacing and margins for better readability
- Professional header with gradient background
- Consistent typography throughout the application

### User Experience Improvements:
- Press Enter to log in (no need to click the button)
- Loading cursor during login
- Better error messages with clearer instructions
- Improved validation feedback
- Color-coded buttons by function:
  - Blue: Primary actions (Create, View)
  - Green: Success actions (Approve, Email)
  - Yellow: Warning actions (Conflicts)
  - Purple: Administrative actions

### Accessibility:
- Larger clickable areas for buttons
- Better contrast ratios for text
- Cursor changes to indicate clickable elements
- Visual feedback on all interactive elements

## Troubleshooting

### Calendar Integration Issues:

**"Google Calendar credentials not configured"**
- Make sure you have a `credentials.json` file in the project root
- Verify the file is valid JSON and contains OAuth 2.0 credentials

**"Failed to sync to Google Calendar"**
- Check your internet connection
- Ensure you've authorized the application
- Try deleting the `tokens` folder and re-authorizing

### Email Reminder Issues:

**"Not receiving daily reminders"**
- Check your email spam folder
- Verify your email address in your user profile
- Ensure the SMTP settings in `.env` are correct
- Check that you have approved timetable entries

**"Authentication failed" email errors**
- For Gmail, use an App Password instead of your regular password
- Enable "Less secure app access" if using other email providers
- Verify SMTP_HOST and SMTP_PORT are correct for your email provider

## Best Practices

1. **Keep your calendar synced**: Re-sync whenever your timetable changes
2. **Check email reminders**: They're sent at 8 AM, so check your email in the morning
3. **Export regularly**: Keep a local .ics backup of your timetable
4. **Update your profile**: Ensure your email address is correct and accessible
5. **Test before semester**: Verify calendar integration works before the semester starts

## Support

For issues or questions:
- Check the logs in the application console
- Review the error messages carefully
- Contact your system administrator
- Refer to the main README.md for general application help

---

© 2025 TimeFlow - Timetable Management System
