def combinationSum(candidates, target):
    result = []

    def backtrack(remaining, combination, start):
        if remaining == 0:
            result.append(list(combination))
            return
        elif remaining < 0:
            return

        for i in range(start, len(candidates)):
            combination.append(candidates[i])
            backtrack(
                remaining - candidates[i], combination, i
            )  # 注意传入 i 而不是 i + 1
            combination.pop()  # 回溯

    backtrack(target, [], 0)
    return result


print(combinationSum([2, 3, 6, 7], 7))
