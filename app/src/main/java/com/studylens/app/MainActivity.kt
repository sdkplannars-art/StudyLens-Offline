package com.studylens.app

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale

private val Ink = Color(0xFF0B1020)
private val Panel = Color(0xFF151D33)
private val Blue = Color(0xFF6C8CFF)
private val Mint = Color(0xFF64E6C2)
private val Muted = Color(0xFFAAB5D0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { StudyLensApp() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyLensApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pages by remember { mutableStateOf(listOf<Uri>()) }
    var subject by remember { mutableStateOf("General") }
    var language by remember { mutableStateOf("Simple English") }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { chosen ->
        if (chosen.isNotEmpty()) { pages = (pages + chosen).distinct(); error = "" }
    }
    val scroll = rememberScrollState()

    MaterialTheme(colorScheme = darkColorScheme(primary = Blue, background = Ink, surface = Panel, onSurface = Color.White)) {
        Scaffold(containerColor = Ink, bottomBar = {
            NavigationBar(containerColor = Color(0xFF10172A), contentColor = Color.White) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.AutoAwesome, null) }, label = { Text("Study") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Forum, null) }, label = { Text("Ask AI") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text("My Notes") })
            }
        }) { pad ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(Blue, Color(0xFF9C70FF))), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoStories, null, tint = Color.White, modifier = Modifier.size(27.dp)) }
                        Column(Modifier.weight(1f)) { Text("StudyLens", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Color.White); Text("Your personal AI study buddy", fontSize = 12.sp, color = Muted) }
                        Surface(color = Color(0xFF1D3040), shape = CircleShape) { Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Bolt, null, tint = Mint, modifier = Modifier.size(15.dp)); Text(" AI", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
                    }
                }
                item {
                    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF273D77), Color(0xFF332657), Color(0xFF17223B))), RoundedCornerShape(26.dp)).padding(20.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("LEARN SMARTER", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                            Text(if (tab == 1) "Ask anything from\nyour textbook." else if (tab == 2) "Your notes,\nall in one place." else "Turn pages into\nA-grade study notes.", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp)
                            Text("Upload page photos. Find key ideas, get clear explanations, and revise with confidence.", color = Color(0xFFD4DCF5), fontSize = 13.sp, lineHeight = 19.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                MiniPill("✦ Key topics"); MiniPill("◉ Page-based answers"); MiniPill("✓ Easy summaries")
                            }
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Your study material", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("${pages.size} pages", color = Muted, fontSize = 12.sp) }
                    Spacer(Modifier.height(10.dp))
                    Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF293653))) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.fillMaxWidth().height(92.dp).background(Color(0xFF1D2945), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                Icon(if (pages.isEmpty()) Icons.Default.AddPhotoAlternate else Icons.Default.Collections, null, tint = Blue, modifier = Modifier.size(42.dp))
                            }
                            Text(if (pages.isEmpty()) "Bring your textbook to life" else "${pages.size} page${if (pages.size == 1) "" else "s"} ready to study", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(if (pages.isEmpty()) "Choose one or more clear page photos to get started." else "Add more pages anytime. Pages are read on your phone; photos are not uploaded.", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                            Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text(if (pages.isEmpty()) "Choose page photos" else "Add more photos", fontWeight = FontWeight.Bold) }
                            if (pages.isNotEmpty()) TextButton(onClick = { pages = emptyList(); result = ""; answer = "" }) { Text("Clear selected pages", color = Muted) }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f)) { Text("Subject", color = Muted, fontSize = 12.sp); Spacer(Modifier.height(6.dp)); DropdownChoice(subject, listOf("General", "Physics", "Chemistry", "Biology", "Mathematics", "Computer Science", "English", "History")) { subject = it } }
                        Column(Modifier.weight(1f)) { Text("Explanation", color = Muted, fontSize = 12.sp); Spacer(Modifier.height(6.dp)); DropdownChoice(language, listOf("Simple English", "Urdu", "Easy English", "Roman Urdu")) { language = it } }
                    }
                }
                item {
                    Button(onClick = {
                        if (pages.isEmpty()) { error = "Add at least one textbook photo first."; return@Button }
                        busy = true; error = ""; answer = ""
                        scope.launch {
                            try { result = withContext(Dispatchers.IO) { analyzePages(context, pages, subject, language) }; tab = 2 }
                            catch (e: Exception) { error = friendlyError(e) }
                            finally { busy = false }
                        }
                    }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)) {
                        if (busy) { CircularProgressIndicator(Modifier.size(20.dp), color = Ink, strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)); Text("Reading your pages…", fontWeight = FontWeight.Bold) }
                        else { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("Find important topics", fontWeight = FontWeight.ExtraBold) }
                    }
                }
                if (error.isNotBlank()) item { Notice(error, true) }
                if (result.isNotBlank() && tab != 1) item {
                    SectionTitle("Your AI study notes", "Created on your phone — no API key needed")
                    Surface(color = Panel, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3B5D))) { Text(result, modifier = Modifier.fillMaxWidth().padding(17.dp), color = Color(0xFFEAF0FF), fontSize = 14.sp, lineHeight = 22.sp) }
                }
                if (tab == 1) {
                    item { SectionTitle("Ask your textbook", "Finds relevant explanations in your scanned text") }
                    item {
                        OutlinedTextField(value = question, onValueChange = { question = it }, modifier = Modifier.fillMaxWidth(), label = { Text("e.g. Explain Newton's second law") }, minLines = 2, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue, unfocusedBorderColor = Color(0xFF394766), focusedLabelColor = Blue, cursorColor = Blue))
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = {
                            if (question.isBlank()) { error = "Type a question first."; return@Button }
                            if (result.isBlank() && pages.isEmpty()) { error = "Upload pages and find topics first."; return@Button }
                            busy = true; error = ""
                            scope.launch { try { answer = withContext(Dispatchers.IO) { askQuestion(question, result, pages, context, language) } } catch (e: Exception) { error = friendlyError(e) } finally { busy = false } }
                        }, enabled = !busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Icon(Icons.Default.Send, null); Spacer(Modifier.width(8.dp)); Text(if (busy) "Thinking…" else "Ask StudyLens") }
                    }
                    if (answer.isNotBlank()) item { SectionTitle("Answer", "Simple, step-by-step explanation"); Surface(color = Panel, shape = RoundedCornerShape(20.dp)) { Text(answer, Modifier.padding(17.dp), color = Color.White, fontSize = 14.sp, lineHeight = 22.sp) } }
                }
                if (tab == 2 && result.isBlank()) item { Notice("Your saved study notes will appear here after you analyze your page photos.", false) }
                item { Text("StudyLens • Learn at your own pace", color = Color(0xFF687796), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable private fun MiniPill(text: String) { Surface(color = Color(0x333F5F9F), shape = CircleShape) { Text(text, color = Color(0xFFE3E9FF), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) } }
@Composable private fun SectionTitle(title: String, subtitle: String) { Column { Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(subtitle, color = Muted, fontSize = 12.sp) } }
@Composable private fun Notice(text: String, isError: Boolean) { Surface(color = if (isError) Color(0xFF3C202E) else Color(0xFF18283A), shape = RoundedCornerShape(14.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (isError) Icons.Default.ErrorOutline else Icons.Default.Info, null, tint = if (isError) Color(0xFFFF91A8) else Mint); Spacer(Modifier.width(10.dp)); Text(text, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp) } } }
@Composable private fun DropdownChoice(value: String, options: List<String>, onSelect: (String) -> Unit) { var expanded by remember { mutableStateOf(false) }; Box {
    Surface(onClick = { expanded = true }, color = Panel, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF303D5A))) { Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) { Text(value, Modifier.weight(1f), color = Color.White, fontSize = 12.sp, maxLines = 1); Icon(Icons.Default.ExpandMore, null, tint = Muted) } }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false }) } }
} }

private fun analyzePages(context: android.content.Context, uris: List<Uri>, subject: String, language: String): String {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val pagesText = mutableListOf<String>()
    try {
        uris.forEachIndexed { index, uri ->
            val image = InputImage.fromFilePath(context, uri)
            val text = Tasks.await(recognizer.process(image)).text.trim()
            if (text.isNotBlank()) pagesText.add("PAGE ${index + 1}\n$text")
        }
    } finally { recognizer.close() }
    if (pagesText.isEmpty()) throw Exception("I couldn't read text in these photos. Try sharper, well-lit pictures with the whole page visible.")
    val fullText = pagesText.joinToString("\n\n")
    return buildOfflineNotes(fullText, subject)
}

private fun buildOfflineNotes(text: String, subject: String): String {
    val lines = text.lines().map { it.trim() }.filter { it.length >= 4 }
    val candidates = lines.filter { line ->
        line.length in 5..150 && !line.startsWith("PAGE ") &&
        (line == line.uppercase(Locale.ROOT) || line.matches(Regex("^\\d+(\\.\\d+)*[.)]?\\s+.+")) ||
         line.endsWith(":") || line.contains("definition", true) || line.contains("formula", true) ||
         line.contains("important", true) || line.contains("law of", true) || line.contains("theorem", true))
    }.distinct().take(12)
    val sentences = text.replace(Regex("(?m)^PAGE \\d+\\s*$"), "")
        .split(Regex("(?<=[.!?])\\s+|\\n"))
        .map { it.trim().replace(Regex("\\s+"), " ") }
        .filter { it.length in 35..280 && it.split(Regex("\\s+")).size >= 6 }
        .distinct()
    val keywords = listOf("define", "definition", "important", "function", "process", "types", "causes", "effects", "formula", "law", "theorem", "example", "advantages", "disadvantages", "characteristics", "properties", "difference", "because", "therefore", "is called", "consists of")
    val ranked = sentences.map { sentence ->
        val lower = sentence.lowercase(Locale.ROOT)
        val score = keywords.count { lower.contains(it) } + (if (sentence.contains("=")) 2 else 0) + (if (sentence.contains(Regex("\\d"))) 1 else 0)
        sentence to score
    }.sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenByDescending { it.first.length }).take(8)
    return buildString {
        append("STUDYLENS OFFLINE STUDY NOTES\n")
        append("Subject: $subject\n\n")
        append("IMPORTANT HEADINGS\n")
        if (candidates.isEmpty()) append("No clear headings detected. Use the key statements below and check your textbook headings manually.\n")
        else candidates.take(8).forEachIndexed { i, heading -> append("${i + 1}. $heading\n") }
        append("\nKEY STATEMENTS TO REVISE\n")
        if (ranked.isEmpty()) append("Very little readable text was detected. Try taking a clearer photo.\n")
        else ranked.forEachIndexed { i, pair -> append("${i + 1}. ${pair.first}\n\n") }
        append("\nQUICK REVISION TIPS\n")
        append("• Learn the definitions and headings first.\n• Practise any formulas, laws, dates, and numbered steps visible on the page.\n• These topics are selected by simple offline text rules, not a predictive exam model; confirm them with your teacher or syllabus.\n")
    }
}

private fun askQuestion(question: String, notes: String, uris: List<Uri>, context: android.content.Context, language: String): String {
    val source = if (notes.isNotBlank()) notes else analyzePages(context, uris, "General", language)
    val stopWords = setOf("what", "which", "when", "where", "who", "whom", "why", "how", "does", "this", "that", "with", "from", "have", "has", "are", "the", "and", "for", "about", "explain", "tell", "me", "please", "can", "you")
    val terms = question.lowercase(Locale.ROOT).split(Regex("[^\\p{L}\\p{N}]+" )).filter { it.length > 2 && it !in stopWords }.toSet()
    val chunks = source.split(Regex("(?<=[.!?])\\s+|\\n")).map { it.trim() }.filter { it.length > 20 }
    val matches = chunks.map { line -> line to terms.count { line.lowercase(Locale.ROOT).contains(it) } }
        .filter { it.second > 0 }.sortedByDescending { it.second }.take(5)
    if (matches.isEmpty()) return "I couldn't find a clear answer to that question in the text I read. Try asking with a keyword or heading from your page, or upload a clearer photo.\n\nOffline mode searches your scanned textbook text; it does not generate new facts like a cloud AI tutor."
    return buildString {
        append("Here are the most relevant parts I found in your textbook:\n\n")
        matches.forEachIndexed { i, pair -> append("${i + 1}. ${pair.first}\n\n") }
        append("\nTip: Ask a shorter question using the exact topic name for more relevant matches. This answer is extracted from your page text, not generated by a cloud AI model.")
    }
}
