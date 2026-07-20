import re

with open('src/main/java/com/example/approvalhierarchy/controller/RequestController.java', 'r') as f:
    content = f.read()

# Replace the block for 'request'
pattern_request = re.compile(
    r'if \(principal != null\) \{\s*'
    r'com\.example\.approvalhierarchy\.model\.User currentUser = userService\.getUserByUsername\(principal\.getName\(\)\)\.orElse\(null\);\s*'
    r'if \(currentUser != null && currentUser\.getEmployee\(\) != null && currentUser\.getEmployee\(\)\.getId\(\)\.equals\(request\.getRequestor\(\)\.getId\(\)\)\) \{\s*'
    r'attributes\.addFlashAttribute\(\"error\", \"You cannot (approve|reject|reassign|escalate) your own request!\"\);\s*'
    r'return \"redirect:/requests\";\s*'
    r'\}\s*'
    r'\}'
)

content = pattern_request.sub(
    'if (!isAuthorizedApprover(request, principal)) {\n                return "error/403";\n            }',
    content
)

# Replace the block for 'req'
pattern_req = re.compile(
    r'if \(principal != null\) \{\s*'
    r'com\.example\.approvalhierarchy\.model\.User currentUser = userService\.getUserByUsername\(principal\.getName\(\)\)\.orElse\(null\);\s*'
    r'if \(currentUser != null && currentUser\.getEmployee\(\) != null && currentUser\.getEmployee\(\)\.getId\(\)\.equals\(req\.getRequestor\(\)\.getId\(\)\)\) \{\s*'
    r'attributes\.addFlashAttribute\(\"error\", \"You cannot (approve|reject|reassign|escalate) your own request!\"\);\s*'
    r'return \"redirect:/requests\";\s*'
    r'\}\s*'
    r'\}'
)

content = pattern_req.sub(
    'if (!isAuthorizedApprover(req, principal)) {\n                return "error/403";\n            }',
    content
)

# Add the helper method at the end
helper_method = '''
    private boolean isAuthorizedApprover(Request request, Principal principal) {
        if (principal == null) return false;
        com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
        if (currentUser == null || currentUser.getEmployee() == null) return false;
        
        Long currentEmpId = currentUser.getEmployee().getId();
        
        // Cannot process own requests
        if (currentEmpId.equals(request.getRequestor().getId())) return false;
        
        // Must be the assigned approver
        if (request.getApprover() == null || !currentEmpId.equals(request.getApprover().getId())) return false;
        
        return true;
    }
}'''

# find the last '}'
last_brace_index = content.rfind('}')
if last_brace_index != -1:
    content = content[:last_brace_index] + helper_method + '\n'

with open('src/main/java/com/example/approvalhierarchy/controller/RequestController.java', 'w') as f:
    f.write(content)
