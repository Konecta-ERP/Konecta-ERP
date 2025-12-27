import re
import json
import os
from datetime import datetime, timedelta  # <-- ADDED 'timedelta'

# --- Define workflow file ---
WORKFLOW_FILE = "workflow_state.json"

# --- Define workflow states ---
STATE_NORMAL = "normal"
# Leave Request States
STATE_AWAITING_LEAVE_ID = "awaiting_leave_id"
STATE_AWAITING_LEAVE_START_DATE = "awaiting_leave_start_date"
STATE_AWAITING_LEAVE_END_DATE = "awaiting_leave_end_date"
STATE_AWAITING_LEAVE_REASON = "awaiting_leave_reason"
# Expense Report States
STATE_AWAITING_EXPENSE_ID = "awaiting_expense_id"
STATE_AWAITING_EXPENSE_AMOUNT = "awaiting_expense_amount"
STATE_AWAITING_EXPENSE_REASON = "awaiting_expense_reason"
# Timesheet Log States
STATE_AWAITING_TIMESHEET_ID = "awaiting_timesheet_id"
STATE_AWAITING_TIMESHEET_DATE = "awaiting_timesheet_date"
STATE_AWAITING_TIMESHEET_HOURS = "awaiting_timesheet_hours"

# --- Define new intent triggers ---
# THIS IS THE LINE THAT IS FIXED
INTENT_TRIGGERS = {
    r"leave request|request leave|request (.*) (off|leave)|vacation time|holiday request|request (.*) holiday|submit leave request|take (.*) leave": "LEAVE_REQUEST",
    r"expense (report|request)|file (an|my) expense|submit (an|my) expense": "EXPENSE_REPORT",
    r"timesheet|log (my|my) hours|update (my|my) time": "TIMESHEET_LOG",
}

def save_workflow_to_json(data: dict, workflow_type: str):
    """
    Reads, updates, and saves the workflow data to the JSON file.
    """
    print(f"Saving to JSON: {data}")
    if not os.path.exists(WORKFLOW_FILE):
        initial_data = {"leave_requests": [], "expenses": [], "timesheets": []}
        with open(WORKFLOW_FILE, 'w') as f:
            json.dump(initial_data, f, indent=2)
    
    with open(WORKFLOW_FILE, 'r') as f:
        all_workflows = json.load(f)
        
    if workflow_type in all_workflows:
        all_workflows[workflow_type].append(data)
    else:
        all_workflows[workflow_type] = [data]

    with open(WORKFLOW_FILE, 'w') as f:
        json.dump(all_workflows, f, indent=2)

def parse_leave_details(query: str, context: dict):
    """
    Parses a query for any leave details and adds them to the context.
    """
    query_lower = query.lower()
    
    # Use re.search to find optional details
    id_match = re.search(r"employee id ([\w\d-]+)", query_lower)
    start_match = re.search(r"from (\d{4}-\d{2}-\d{2})", query_lower)
    end_match = re.search(r"to (\d{4}-\d{2}-\d{2})", query_lower)
    reason_match = re.search(r"reason:? (.*)", query_lower) # Added optional colon
    
    # Update context ONLY if the field doesn't exist yet
    if not context.get('employee_id') and id_match:
        context['employee_id'] = id_match.group(1)
    
    if not context.get('start_date') and start_match:
        context['start_date'] = start_match.group(1)
        
    if not context.get('end_date') and end_match:
        context['end_date'] = end_match.group(1)
        
    if not context.get('reason') and reason_match:
        context['reason'] = reason_match.group(1).strip()
        
    # Handle simple reason (like "reason christmas")
    if not context.get('reason') and "reason" in query_lower and not reason_match:
        simple_reason = re.search(r"reason ([\w\s]+)", query_lower)
        if simple_reason:
             context['reason'] = simple_reason.group(1).strip()

    # --- NEW: Relative Date Logic ---
    if not context.get('start_date') and "tomorrow" in query_lower:
        tomorrow_date = (datetime.now() + timedelta(days=1)).strftime('%Y-%m-%d')
        context['start_date'] = tomorrow_date
        print(f"Parsed 'tomorrow' as start date: {tomorrow_date}")
        # If they only say "tomorrow", assume it's a 1-day leave
        if not context.get('end_date'):
            context['end_date'] = tomorrow_date
            print(f"Parsed 'tomorrow' as end date: {tomorrow_date}")
            
    return context

def handle_leave_workflow(query: str, current_state: str, context: dict):
    """
    Handles the slot-filling logic for a leave request.
    """
    # 1. Add the user's latest reply to the context
    if current_state == STATE_AWAITING_LEAVE_ID:
        context['employee_id'] = query
    elif current_state == STATE_AWAITING_LEAVE_START_DATE:
        context['start_date'] = query
    elif current_state == STATE_AWAITING_LEAVE_END_DATE:
        context['end_date'] = query
    elif current_state == STATE_AWAITING_LEAVE_REASON:
        context['reason'] = query
    
    # 2. Check for missing slots in order
    if not context.get('employee_id'):
        new_state = STATE_AWAITING_LEAVE_ID
        response = "I have some details for your leave. To continue, **What is your Employee ID?**"
    elif not context.get('start_date'):
        new_state = STATE_AWAITING_LEAVE_START_DATE
        response = f"Thank you, {context['employee_id']}. **What is the start date?** (e.g., YYYY-MM-DD)"
    elif not context.get('end_date'):
        new_state = STATE_AWAITING_LEAVE_END_DATE
        response = "Got it. **What is the end date?** (e.g., YYYY-MM-DD)"
    elif not context.get('reason'):
        new_state = STATE_AWAITING_LEAVE_REASON
        response = "Almost done. **What is the reason for this leave?**"
    else:
        # 3. All slots are full!
        final_request = {
            "employee_id": context.get('employee_id'),
            "start_date": context.get('start_date'),
            "end_date": context.get('end_date'),
            "reason": context.get('reason'),
            "status": "submitted",
            "timestamp": datetime.now().isoformat()
        }
        save_workflow_to_json(final_request, "leave_requests")
        response = f"Thank you! Your leave request for Employee ID **{final_request['employee_id']}** from **{final_request['start_date']}** to **{final_request['end_date']}** has been submitted."
        new_state = STATE_NORMAL
        context = {} # Clear context

    return (response, new_state, context)

# --- (Add similar handlers for expense and timesheet) ---
# ... def handle_expense_workflow(...):
# ... def handle_timesheet_workflow(...):

def handle_workflow_step(query: str, current_state: str, context: dict):
    """
    Manages the multi-step workflow conversation.
    """
    query_lower = query.lower()
    
    # --- Universal Cancel ---
    if query_lower == 'cancel':
        return ("Workflow cancelled. How can I help you?", STATE_NORMAL, {})

    # === STATE: NORMAL (Checking for NEW intents) ===
    if current_state == STATE_NORMAL:
        for regex_pattern, intent_type in INTENT_TRIGGERS.items():
            if re.search(regex_pattern, query_lower):
                print(f"Workflow intent detected: {intent_type}")
                
                # --- LEAVE REQUEST ---
                if intent_type == "LEAVE_REQUEST":
                    # Parse all details from this first query
                    context = parse_leave_details(query, {})
                    # Now, start the slot-filling process
                    return handle_leave_workflow(query, STATE_NORMAL, context)
                
                # --- EXPENSE REPORT ---
                elif intent_type == "EXPENSE_REPORT":
                    # context = parse_expense_details(query, {})
                    # return handle_expense_workflow(query, STATE_NORMAL, context)
                    response = "I can help you file an expense report. **What is your Employee ID?**"
                    return (response, STATE_AWAITING_EXPENSE_ID, {})

                # --- TIMESHEET LOG ---
                elif intent_type == "TIMESHEET_LOG":
                    # context = parse_timesheet_details(query, {})
                    # return handle_timesheet_workflow(query, STATE_NORMAL, context)
                    response = "I can log your hours. **What is your Employee ID?**"
                    return (response, STATE_AWAITING_TIMESHEET_ID, {})
        
        # No intent detected, so return None for RAG
        return (None, STATE_NORMAL, context)

    # === STATE: ALREADY IN A WORKFLOW ===
    
    # --- LEAVE REQUEST ---
    if current_state in [STATE_AWAITING_LEAVE_ID, STATE_AWAITING_LEAVE_START_DATE, STATE_AWAITING_LEAVE_END_DATE, STATE_AWAITING_LEAVE_REASON]:
        return handle_leave_workflow(query, current_state, context)

    # --- EXPENSE REPORT ---
    if current_state in [STATE_AWAITING_EXPENSE_ID, STATE_AWAITING_EXPENSE_AMOUNT, STATE_AWAITING_EXPENSE_REASON]:
        if current_state == STATE_AWAITING_EXPENSE_ID:
             context['employee_id'] = query
             return ("Got it. **What is the expense amount?**", STATE_AWAITING_EXPENSE_AMOUNT, context)
        # ... (add other steps for amount and reason) ...
        elif current_state == STATE_AWAITING_EXPENSE_AMOUNT:
            context['amount'] = query
            return ("Thanks. **What is the reason for this expense?**", STATE_AWAITING_EXPENSE_REASON, context)
        elif current_state == STATE_AWAITING_EXPENSE_REASON:
            context['reason'] = query
            # (save to json)
            return (f"Expense report for {context['amount']} submitted.", STATE_NORMAL, {})


    # --- TIMESHEET LOG ---
    if current_state in [STATE_AWAITING_TIMESHEET_ID, STATE_AWAITING_TIMESHEET_DATE, STATE_AWAITING_TIMESHEET_HOURS]:
        if current_state == STATE_AWAITING_TIMESHEET_ID:
             context['employee_id'] = query
             return ("Thank you. **For what date are you logging hours?**", STATE_AWAITING_TIMESHEET_DATE, context)
        # ... (add other steps for date and hours) ...
        elif current_state == STATE_AWAITING_TIMESHEET_DATE:
            context['date'] = query
            return ("Got it. **How many hours did you work?**", STATE_AWAITING_TIMESHEET_HOURS, context)
        elif current_state == STATE_AWAITING_TIMESHEET_HOURS:
            context['hours'] = query
            # (save to json)
            return (f"{context['hours']} hours logged for {context['date']}.", STATE_NORMAL, {})


    # Fallback if state is unknown (shouldn't happen)
    return (None, STATE_NORMAL, context)
