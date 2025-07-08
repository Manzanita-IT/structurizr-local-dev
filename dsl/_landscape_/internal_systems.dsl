/**
 * To make everything work correctly, it is important to ensure that the name assigned to the
 * software system (the name is on the right in quotation marks) matches the name you give to the
 * workspace for that system. So,
 *
 * WHEN
 *   appX = softwareSystem "Application X"
 * THEN
 *   folder appX
 *   and appX/C1_context.dsl contains:
 *     workspace extends ../C0_landscape.dsl {
 *       name "Application X"
 *       ...
 *     }
 */

internal = softwareSystem "Internal"
