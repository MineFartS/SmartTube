from philh_myftp_biz.pc import Path
import re, sys

# Java Regex Patterns
# Matches method/field declarations that don't already have public/private/protected
JAVA_BLANK_MODIFIER = re.compile(
    r'(?P<indent>^\s*)(?!(?:public|private|protected|return|throw)\b)(?P<rest>(?:static\s+|final\s+|synchronized\s+|abstract\s+)*(?:[\w<>\[\]]+)\s+\w+\s*(?:;|=|\())', 
    re.MULTILINE
)
JAVA_PACKAGE_PRIVATE = re.compile(r'\b(protected|private)\b')

def make_java_public(content):
    # 1. Replace existing private/protected modifiers with public
    content = JAVA_PACKAGE_PRIVATE.sub('public', content)
    
    # 2. Add public to package-private definitions (lines missing an explicit modifier)
    # Note: A pure regex approach has limits with edge cases; an AST parser is ideal for production.
    def java_replacer(match):
        return f"{match.group('indent')}public {match.group('rest')}"
    
    content = JAVA_BLANK_MODIFIER.sub(java_replacer, content)
    return content

import tree_sitter_kotlin as kt
from tree_sitter import Language, Parser

def make_kotlin_public(content: str) -> str:
    # Initialize Tree-sitter
    KT_LANGUAGE = Language(kt.language())
    parser = Parser(KT_LANGUAGE)
    tree = parser.parse(bytes(content, "utf8"))
    root_node = tree.root_node

    TARGET_NODE_TYPES = {
        'class_declaration',
        'interface_declaration',
        'object_declaration',
        'function_declaration',
        'property_declaration'
    }

    modifications = [] # List of tuples: (start_byte, end_byte, replacement_text)

    def walk_tree(node, inside_function=False):
        nonlocal modifications
        
        current_inside_function = inside_function
        if node.type == 'function_declaration':
            current_inside_function = True

        if node.type in TARGET_NODE_TYPES and not inside_function:
            node_text = content[node.start_byte:node.end_byte]
            
            # Check if this declaration is an override; if so, skip it completely
            # (Overrides inherit their visibility from the parent class/interface)
            if 'modifiers' in [child.type for child in node.children]:
                modifiers_node = next(child for child in node.children if child.type == 'modifiers')
                modifiers_text = content[modifiers_node.start_byte:modifiers_node.end_byte]
                if 'override' in modifiers_text:
                    for child in node.children:
                        walk_tree(child, current_inside_function)
                    return

            # Check if a visibility modifier already exists anywhere in this node's header
            # We look for visibility keywords before the core keyword (class/fun/val/var)
            header_match = re.search(r'\b(private|protected|internal|public)\b', node_text)
            
            if header_match:
                visibility_word = header_match.group(1)
                # If it's already public, do nothing
                if visibility_word == 'public':
                    pass
                else:
                    # If it's private/protected/internal, replace it with public exactly where it stands
                    start = node.start_byte + header_match.start(1)
                    end = node.start_byte + header_match.end(1)
                    modifications.append((start, end, "public"))
            else:
                # No visibility modifier exists. We must insert 'public' at the very beginning 
                # of the declaration keywords (before const, expect, actual, etc., but after indentation)
                keyword_match = re.search(r'\b(class|interface|object|fun|val|var|const|expect|actual)\b', node_text)
                if keyword_match:
                    insert_pos = node.start_byte + keyword_match.start(1)
                    modifications.append((insert_pos, insert_pos, "public "))

        for child in node.children:
            walk_tree(child, current_inside_function)

    # Walk the tree to gather all coordinates
    walk_tree(root_node)

    # Sort modifications from the end of the file to the front to keep byte offsets valid
    # If starting positions are equal, sort by end position descending
    modifications.sort(key=lambda x: (x[0], x[1]), reverse=True)

    # Apply the edits safely
    content_bytes = bytearray(content, "utf8")
    for start, end, replacement in modifications:
        content_bytes[start:end] = bytes(replacement, "utf8")

    return content_bytes.decode("utf8")

for file in Path(sys.argv[1]).descendants:

    if file.ext not in ['java', 'kt']:
        continue

    print(f'Modifying:', file)

    with file.open() as f:
        content = f.read()

    match file.ext:

        case 'java':
            content = make_java_public(content)

        case 'kt':
            content = make_kotlin_public(content)

    with file.open('w') as f:
        f.write(content)


