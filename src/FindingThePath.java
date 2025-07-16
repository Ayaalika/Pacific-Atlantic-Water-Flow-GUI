import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindingThePath {
    private int n, m;
    private Cell[][] G;
    private boolean[][] pacific, atlantic;
    private List<Cell> path;

    public FindingThePath(Cell[][] G) {
        this.G = G;
        this.n = G.length;
        this.m = G[0].length;
        this.pacific = new boolean[n][m];
        this.atlantic = new boolean[n][m];
        this.path = new ArrayList<>();
    }

    public List<Cell> solve() {
        for (boolean[] row : pacific) Arrays.fill(row, false);
        for (boolean[] row : atlantic) Arrays.fill(row, false);
        path.clear();

        for (int j = 0; j < m; j++) dfs(0, j, pacific);
        for (int i = 0; i < n; i++) dfs(i, 0, pacific);
        for (int j = 0; j < m; j++) dfs(n - 1, j, atlantic);
        for (int i = 0; i < n; i++) dfs(i, m - 1, atlantic);

        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                if (pacific[i][j] && atlantic[i][j])
                    path.add(G[i][j]);

        return path;
    }

    private void dfs(int i, int j, boolean[][] vis) {
        if (G[i][j].isDesert || vis[i][j]) return;
        vis[i][j] = true;

        int[] rowDirections = {-1, 1, 0, 0};
        int[] colDirections = {0, 0, -1, 1};
        for (int d = 0; d < 4; d++) {
            int newRow = i + rowDirections[d], newCol = j + colDirections[d];
            if (newRow >= 0 && newRow < n && newCol >= 0 && newCol < m && !vis[newRow][newCol]) {
                if (G[newRow][newCol].height > G[i][j].height || G[newRow][newCol].isRiver)
                    dfs(newRow, newCol, vis);
            }
        }
    }
}
