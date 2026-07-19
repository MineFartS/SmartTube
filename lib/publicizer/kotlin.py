import re

# Kotlin Regex Patterns
# Matches internal, private, or protected visibility modifiers
KOTLIN_MODIFIERS = re.compile(r'\b(internal|private|protected)\b')

def rewrite(content: str) -> str:
    return KOTLIN_MODIFIERS.sub('public', content)
